package com.mattbobambrose.mcp_utils.shell

import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.core.JsonValue
import com.openai.models.ChatModel
import com.openai.models.FunctionDefinition
import com.openai.models.FunctionParameters
import com.openai.models.chat.completions.ChatCompletionAssistantMessageParam
import com.openai.models.chat.completions.ChatCompletionCreateParams
import com.openai.models.chat.completions.ChatCompletionFunctionTool
import com.openai.models.chat.completions.ChatCompletionTool
import com.openai.models.chat.completions.ChatCompletionToolMessageParam
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.sse.SSE
import io.ktor.serialization.kotlinx.json.json
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.mcpSse
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.Tool
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.util.Collections.emptyList
import kotlin.jvm.optionals.getOrNull

/**
 * Converts a list of `Tool` objects into a corresponding list of `ChatCompletionTool` objects.
 * Each `Tool` object is transformed into a `ChatCompletionTool` by building its `FunctionDefinition`
 * and associating it with the defined parameters.
 *
 * @return A list of `ChatCompletionTool` objects created based on the input list of `Tool` objects.
 */
private fun List<Tool>.toListChatCompletionTools(): List<ChatCompletionTool> {
  return this.map { tool ->
    ChatCompletionTool.ofFunction(
      ChatCompletionFunctionTool.builder()
        .function(
          FunctionDefinition.builder()
            .name(tool.name)
            .description(tool.description ?: "")
            .parameters(
              FunctionParameters.builder()
                .putAdditionalProperty("type", JsonValue.from(tool.inputSchema.type))
                .putAdditionalProperty("properties", JsonValue.from(tool.inputSchema.properties))
                .putAdditionalProperty("required", JsonValue.from(tool.inputSchema.required))
                .build()
            )
            .build()
        )
        .build()
    )
  }
}

/**
 * A client for interacting with the MCP (Model Context Protocol) server.
 * Handles connection establishment, tool management, and query processing using OpenAI models.
 */
class MCPShell : AutoCloseable {
  lateinit var mcpClient: Client
  lateinit var httpClient: HttpClient
  lateinit var availableTools: List<ChatCompletionTool>

  // Configures using the `OPENAI_API_KEY`, `OPENAI_ORG_ID` and `OPENAI_PROJECT_ID` environment variables
  val openAiClient: OpenAIClient = OpenAIOkHttpClient.fromEnv()

  val serverVersion: Implementation
    get() = mcpClient.serverVersion ?: error("Server version is not available")

  /**
   * Establishes a connection to the specified server using Server-Sent Events (SSE).
   * Configures the HTTP client with content negotiation for JSON and SSE capabilities.
   * After establishing the connection, it retrieves and processes a list of available tools on the server.
   *
   * @param serverUrl The URL of the server to connect to.
   */
  suspend fun connectToServer(serverUrl: String) {
    httpClient = HttpClient {
      install(SSE) {
        showCommentEvents()
      }
      install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; prettyPrint = true })
      }
    }

    // Connect to the MCP server via SSE. The mcpSse function performs a handshake.
    mcpClient = httpClient.mcpSse(serverUrl)
    println("Connected to the MCP server: ${mcpClient.serverVersion}")

    // Getting a list of available tools
    val tools = mcpClient.listTools()?.tools
    availableTools = tools?.toListChatCompletionTools() ?: emptyList()
    println("Available tools: ${tools?.map { it.name } ?: "No tools found"}")
  }

  /**
   * Processes a user query by interacting with the OpenAI API and supported tools.
   * Generates a response by calling tools as needed and appending the results to the conversation.
   *
   * @param query The user-provided input to be processed.
   * @return The formatted response obtained from the conversational model and tool usage.
   */
  suspend fun processQuery(
    query: String,
    systemPrompt: String = "You are a helpful assistant that answers questions."
  ): String {
    // Set up parameters builder for the OpenAI chat completion
    val paramsBuilder = ChatCompletionCreateParams.builder()
      .addSystemMessage(systemPrompt)
      .addUserMessage(query)
      .model(ChatModel.GPT_4O)
      .maxCompletionTokens(1000)
      .tools(availableTools)

    val answer = StringBuilder()

    // Get the initial completion response
    var completion = openAiClient.chat().completions().create(paramsBuilder.build())

    // Handle tool calls in a loop until no more tool calls are returned
    while (true) {
      val choice = completion.choices().firstOrNull() ?: break
      println("Choice: $choice")

      // Append direct content from the assistant
      choice.message().content().ifPresent {
        println("Content: $it")
        answer.appendLine(it)
      }

      val toolCalls = choice.message().toolCalls().getOrNull()
      println("Tool calls size: ${toolCalls?.size}")
      println("Tool calls: $toolCalls")

      if (toolCalls.isNullOrEmpty()) {
        break
      }

      // Add the assistant message with tool calls to the conversation
      paramsBuilder.addMessage(
        ChatCompletionAssistantMessageParam.builder()
          .toolCalls(toolCalls)
          .build()
      )

      // Process each tool call and add the results
      for (toolCall in toolCalls) {
        val functionToolCall = toolCall.asFunction()
        val toolName = functionToolCall.function().name()
        val args = Json {}.decodeFromString<Map<String, String>>(functionToolCall.function().arguments())

        val aiMessage = "Calling tool $toolName with arguments $args"
        answer.appendLine("[$aiMessage]")

        // Call the MCP tool with parsed arguments
        val result = mcpClient.callTool(toolName, args)
        val resultContent = result.content.joinToString()
        println("Function result = $resultContent")

        // Add the tool result message
        paramsBuilder.addMessage(
          ChatCompletionToolMessageParam.builder()
            .toolCallId(functionToolCall.id())
            .content(resultContent)
            .build()
        )
      }

      // Request a new response after tool usage
      completion = openAiClient.chat().completions().create(paramsBuilder.build())
    }

    val finalAnswer = answer.toString()
    println("Final answer: $finalAnswer")
    return finalAnswer
  }


  /**
   * Closes the resources used by the `MCPClient` instance.
   */
  override fun close() = runBlocking {
    mcpClient.close()
    httpClient.close()
  }
}

fun main() {
  MCPShell().use { shell ->
    runBlocking {
//      val query = "use my tools to find the cities in california."
      val query = "Use my tools to find the hottest city in California."
//      val query = "Use my tools to find the temperatures of Diablo, Danville, and San Francisco."
      shell.connectToServer("http://localhost:8080/")
      shell.processQuery(query)
    }
  }
}