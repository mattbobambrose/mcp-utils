# Shell Module

MCP client for connecting to MCP servers and processing queries using OpenAI's API with automatic tool calling
capabilities.

## Overview

The shell module provides:

- MCP client implementation using Server-Sent Events (SSE)
- Integration with OpenAI's API for natural language processing
- Automatic tool calling and response handling
- Support for complex multi-step tool execution workflows

## Quick Start

### 1. Set Up Environment

```bash
export OPENAI_API_KEY="your-openai-api-key"
# Optional:
export OPENAI_ORG_ID="your-organization-id"
export OPENAI_PROJECT_ID="your-project-id"
```

### 2. Basic Usage

```kotlin
import com.mattbobambrose.mcp_utils.MCPShell

fun main() {
  MCPShell().use { shell ->
    runBlocking {
      // Connect to MCP server
      shell.connectToServer("http://localhost:8080/")

      // Process a query
      val response = shell.processQuery("What's the weather like in San Francisco?")
      println(response)
    }
  }
}
```

## API Reference

### MCPShell Class

The main client class for interacting with MCP servers.

```kotlin
class MCPShell : AutoCloseable {
  suspend fun connectToServer(serverUrl: String)
  suspend fun processQuery(
    query: String,
    systemPrompt: String = "You are a helpful assistant that answers questions."
  ): String
  override fun close()
}
```

### Methods

#### connectToServer()

```kotlin
suspend fun connectToServer(serverUrl: String)
```

Establishes a connection to an MCP server using Server-Sent Events.

**Parameters:**

- `serverUrl`: The URL of the MCP server to connect to

**Example:**

```kotlin
shell.connectToServer("http://localhost:8080/")
shell.connectToServer("https://api.example.com/mcp")
```

#### processQuery()

```kotlin
suspend fun processQuery(
  query: String,
  systemPrompt: String = "You are a helpful assistant that answers questions."
): String
```

Processes a user query using OpenAI's API and available MCP tools.

**Parameters:**

- `query`: The user's question or request
- `systemPrompt`: Custom system prompt to guide the AI's behavior

**Returns:** The AI's response as a string

**Example:**

```kotlin
val response = shell.processQuery(
  "Find the temperature in London and New York",
  "You are a weather assistant. Always include units in temperature responses."
)
```

## Configuration

### Environment Variables

| Variable            | Required | Description                 |
|---------------------|----------|-----------------------------|
| `OPENAI_API_KEY`    | Yes      | Your OpenAI API key         |
| `OPENAI_ORG_ID`     | No       | Your OpenAI organization ID |
| `OPENAI_PROJECT_ID` | No       | Your OpenAI project ID      |

### OpenAI Model Settings

The shell uses the following OpenAI configuration:

- **Model:** GPT-4o
- **Max Completion Tokens:** 1000
- **Tool Support:** Enabled

## Examples

### Basic Weather Query

```kotlin
MCPShell().use { shell ->
  runBlocking {
    shell.connectToServer("http://localhost:8080/")

    val response = shell.processQuery("What's the weather like in Tokyo?")
    println(response)
  }
}
```

### Custom System Prompt

```kotlin
MCPShell().use { shell ->
  runBlocking {
    shell.connectToServer("http://localhost:8080/")

    val response = shell.processQuery(
      "Analyze the weather patterns in California",
      "You are a meteorologist. Provide detailed analysis with scientific explanations."
    )
    println(response)
  }
}
```

### Complex Multi-Tool Query

```kotlin
MCPShell().use { shell ->
  runBlocking {
    shell.connectToServer("http://localhost:8080/")

    // This might trigger multiple tool calls
    val response = shell.processQuery(
      "Compare the weather in San Francisco, Los Angeles, and San Diego, then tell me which city has the best weather today."
    )
    println(response)
  }
}
```

### Error Handling

```kotlin
MCPShell().use { shell ->
  runBlocking {
    try {
      shell.connectToServer("http://localhost:8080/")
      val response = shell.processQuery("Your query here")
      println(response)
    } catch (e: Exception) {
      println("Error: ${e.message}")
    }
  }
}
```

## How It Works

### 1. Connection Process

1. **HTTP Client Setup**: Configures Ktor HTTP client with SSE and JSON support
2. **MCP Handshake**: Establishes connection using `httpClient.mcpSse(serverUrl)`
3. **Tool Discovery**: Retrieves available tools from the server using `mcpClient.listTools()`
4. **OpenAI Integration**: Converts MCP tools to OpenAI function format

### 2. Query Processing Workflow

1. **Message Setup**: Creates conversation with system prompt and user query
2. **OpenAI API Call**: Sends request to OpenAI with available tools
3. **Tool Execution**: If OpenAI requests tool calls:
    - Calls MCP server tools with parsed arguments
    - Adds tool results to conversation
    - Requests follow-up response from OpenAI
4. **Response Building**: Combines AI responses and tool execution results

### 3. Tool Calling Chain

The shell supports complex tool calling chains:

```
User Query → OpenAI → Tool Call 1 → Tool Result 1 → OpenAI → Tool Call 2 → Tool Result 2 → Final Response
```

## Advanced Usage

### Custom HTTP Client Configuration

```kotlin
class CustomMCPShell : MCPShell() {
  override suspend fun connectToServer(serverUrl: String) {
    httpClient = HttpClient {
      install(SSE) {
        showCommentEvents()
      }
      install(ContentNegotiation) {
        json(Json {
          ignoreUnknownKeys = true
          prettyPrint = true
          // Add custom JSON configuration
        })
      }
      // Add custom client configuration
      install(HttpTimeout) {
        requestTimeoutMillis = 30000
      }
    }

    mcpClient = httpClient.mcpSse(serverUrl)
    // ... rest of connection logic
  }
}
```

### Batch Processing

```kotlin
MCPShell().use { shell ->
  runBlocking {
    shell.connectToServer("http://localhost:8080/")

    val queries = listOf(
      "Weather in New York",
      "Weather in London",
      "Weather in Tokyo"
    )

    val responses = queries.map { query ->
      shell.processQuery(query)
    }

    responses.forEach(::println)
  }
}
```

### Server Information

```kotlin
MCPShell().use { shell ->
  runBlocking {
    shell.connectToServer("http://localhost:8080/")

    println("Connected to: ${shell.serverVersion}")
    println("Available tools: ${shell.availableTools.size}")
  }
}
```

## Integration Examples

### Spring Boot Integration

```kotlin
@Service
class MCPService {
  private val shell = MCPShell()

  @PostConstruct
  fun init() {
    runBlocking {
      shell.connectToServer("http://localhost:8080/")
    }
  }

  suspend fun processUserQuery(query: String): String {
    return shell.processQuery(query)
  }

  @PreDestroy
  fun cleanup() {
    shell.close()
  }
}
```

### CLI Application

```kotlin
fun main(args: Array<String>) {
  if (args.isEmpty()) {
    println("Usage: mcp-shell <server-url>")
    return
  }

  MCPShell().use { shell ->
    runBlocking {
      shell.connectToServer(args[0])

      println("Connected to MCP server. Type 'exit' to quit.")

      while (true) {
        print("> ")
        val input = readlnOrNull() ?: break

        if (input.lowercase() == "exit") break

        try {
          val response = shell.processQuery(input)
          println(response)
        } catch (e: Exception) {
          println("Error: ${e.message}")
        }
      }
    }
  }
}
```

## Troubleshooting

### Common Issues

1. **Connection Failed**
    - Check if MCP server is running
    - Verify server URL is correct
    - Ensure network connectivity

2. **OpenAI API Errors**
    - Verify `OPENAI_API_KEY` is set correctly
    - Check API key permissions
    - Ensure sufficient API credits

3. **Tool Execution Errors**
    - Check server logs for tool execution issues
    - Verify tool parameters are correct
    - Ensure MCP server tools are working

### Debug Mode

Enable debug logging by setting system properties:

```kotlin
System.setProperty("io.ktor.client.logging.level", "ALL")
```

## Performance Considerations

- **Connection Reuse**: Keep `MCPShell` instances alive for multiple queries
- **Tool Caching**: Available tools are cached after initial connection
- **Memory Management**: Always use `use {}` blocks or call `close()` explicitly
- **Concurrency**: `MCPShell` is designed for sequential use; use separate instances for concurrent operations

## Testing

### Unit Tests

```kotlin
@Test
fun testBasicQuery() = runBlocking {
    MCPShell().use { shell ->
      shell.connectToServer("http://localhost:8080/")
      val response = shell.processQuery("test query")
      assertNotNull(response)
    }
  }
```

### Integration Tests

```kotlin
@Test
fun testToolCalling() = runBlocking {
    // Requires running MCP server with tools
    MCPShell().use { shell ->
      shell.connectToServer("http://localhost:8080/")
      val response = shell.processQuery("Use tools to get weather information")
      assertTrue(response.contains("weather") || response.contains("temperature"))
    }
  }
```

Run tests:

```bash
./gradlew :shell:test
```