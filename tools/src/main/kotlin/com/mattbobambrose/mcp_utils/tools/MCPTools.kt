package com.mattbobambrose.mcp_utils.tools// Static imports for TypeNames constants
import com.mattbobambrose.mcp_utils.tools.TypeNames.JAVA_BOOLEAN
import com.mattbobambrose.mcp_utils.tools.TypeNames.JAVA_DOUBLE
import com.mattbobambrose.mcp_utils.tools.TypeNames.JAVA_FLOAT
import com.mattbobambrose.mcp_utils.tools.TypeNames.JAVA_INTEGER
import com.mattbobambrose.mcp_utils.tools.TypeNames.JAVA_LIST
import com.mattbobambrose.mcp_utils.tools.TypeNames.JAVA_MAP
import com.mattbobambrose.mcp_utils.tools.TypeNames.JAVA_SET
import com.mattbobambrose.mcp_utils.tools.TypeNames.JAVA_STRING
import com.mattbobambrose.mcp_utils.tools.TypeNames.KOTLIN_LIST
import com.mattbobambrose.mcp_utils.tools.TypeNames.KOTLIN_MAP
import com.mattbobambrose.mcp_utils.tools.TypeNames.KOTLIN_SET
import com.mattbobambrose.mcp_utils.tools.TypeNames.PRIMITIVE_BOOLEAN
import com.mattbobambrose.mcp_utils.tools.TypeNames.PRIMITIVE_DOUBLE
import com.mattbobambrose.mcp_utils.tools.TypeNames.PRIMITIVE_FLOAT
import com.mattbobambrose.mcp_utils.tools.TypeNames.PRIMITIVE_INT
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaType

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class LLMTool(val description: String)

// Type name constants for consistency
private object TypeNames {
  const val JAVA_STRING = "java.lang.String"
  const val JAVA_INTEGER = "java.lang.Integer"
  const val PRIMITIVE_INT = "int"
  const val JAVA_BOOLEAN = "java.lang.Boolean"
  const val PRIMITIVE_BOOLEAN = "boolean"
  const val JAVA_DOUBLE = "java.lang.Double"
  const val PRIMITIVE_DOUBLE = "double"
  const val JAVA_FLOAT = "java.lang.Float"
  const val PRIMITIVE_FLOAT = "float"
  const val JAVA_LIST = "java.util.List"
  const val KOTLIN_LIST = "kotlin.collections.List"
  const val JAVA_SET = "java.util.Set"
  const val KOTLIN_SET = "kotlin.collections.Set"
  const val JAVA_MAP = "java.util.Map"
  const val KOTLIN_MAP = "kotlin.collections.Map"
}

fun Server.integrateTools(toolsObject: Any) {
  // I am here
  val objectClass = toolsObject::class

  objectClass.functions.forEach { function ->
    val llmToolAnnotation = function.findAnnotation<LLMTool>()
    if (llmToolAnnotation != null) {
      val toolName = function.name
      val description = llmToolAnnotation.description

      // Build input schema from method parameters
      val properties = mutableMapOf<String, JsonPrimitive>()
      val required = mutableListOf<String>()

      function.valueParameters.forEach { param ->
        val paramName = param.name ?: "unknown"
        val javaTypeName = param.type.javaType.typeName
        val paramType = when {
          javaTypeName == JAVA_STRING -> "string"
          javaTypeName in listOf(PRIMITIVE_INT, JAVA_INTEGER) -> "integer"
          javaTypeName in listOf(PRIMITIVE_BOOLEAN, JAVA_BOOLEAN) -> "boolean"
          javaTypeName in listOf(PRIMITIVE_DOUBLE, JAVA_DOUBLE, PRIMITIVE_FLOAT, JAVA_FLOAT) -> "number"
          javaTypeName.startsWith(JAVA_LIST) ||
              javaTypeName.startsWith(JAVA_SET) ||
              javaTypeName.startsWith(KOTLIN_LIST) ||
              javaTypeName.startsWith(KOTLIN_SET) -> "array"

          javaTypeName.startsWith(JAVA_MAP) ||
              javaTypeName.startsWith(KOTLIN_MAP) -> "object"

          else -> "string" // Default to string for unknown types
        }
        properties[paramName] = JsonPrimitive(paramType)
        if (!param.isOptional) {
          required.add(paramName)
        }
      }

      val inputSchema = Tool.Input(
        properties = JsonObject(properties),
        required = required
      )

      // Add the tool to the server
      addTool(
        name = toolName,
        description = description,
        inputSchema = inputSchema
      ) { request ->
        // Prepare arguments for method invocation
        val args = function.valueParameters.map { param ->
          val paramName = param.name ?: "unknown"
          val argValue = request.arguments[paramName]
          val javaTypeName = param.type.javaType.typeName

          when {
            javaTypeName == JAVA_STRING
              -> argValue?.jsonPrimitive?.content

            javaTypeName in listOf(PRIMITIVE_INT, JAVA_INTEGER)
              -> argValue?.jsonPrimitive?.content?.toIntOrNull()

            javaTypeName in listOf(PRIMITIVE_BOOLEAN, JAVA_BOOLEAN)
              -> argValue?.jsonPrimitive?.content?.toBooleanStrictOrNull()

            javaTypeName in listOf(PRIMITIVE_DOUBLE, JAVA_DOUBLE, PRIMITIVE_FLOAT, JAVA_FLOAT)
              -> argValue?.jsonPrimitive?.content?.toDoubleOrNull()

            javaTypeName.startsWith(JAVA_LIST) || javaTypeName.startsWith(KOTLIN_LIST)
              -> {
              val jsonContent = argValue?.jsonPrimitive?.content ?: "[]"
              Json.decodeFromString<List<String>>(jsonContent)
            }

            javaTypeName.startsWith(JAVA_SET) || javaTypeName.startsWith(KOTLIN_SET)
              -> {
              val jsonContent = argValue?.jsonPrimitive?.content ?: "[]"
              Json.decodeFromString<Set<String>>(jsonContent)
            }

            // TODO Write method that tests this
            javaTypeName.startsWith(JAVA_MAP) || javaTypeName.startsWith(KOTLIN_MAP)
              -> argValue?.jsonObject?.mapValues { it.value.jsonPrimitive.content }

            else -> argValue?.jsonPrimitive?.content
          }
        }.toTypedArray<Any?>()

        try {
          // Invoke the method on the tools object
          val result = function.call(toolsObject, *args)

          // Convert result to CallToolResult
          val content = when (result) {
            is List<*> -> result.map { TextContent(it.toString()) }
            is Set<*> -> result.map { TextContent(it.toString()) }
            is String -> listOf(TextContent(result))
            else -> listOf(TextContent(result.toString()))
          }

          CallToolResult(content = content)
        } catch (e: Exception) {
          CallToolResult(
            content = listOf(TextContent("Error executing tool: ${e.message}")),
            isError = true
          )
        }
      }
    }
  }
}