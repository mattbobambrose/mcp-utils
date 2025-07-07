# Tools Module

Automatic tool registration for MCP servers using Kotlin reflection. This module provides utilities to automatically
expose Kotlin methods as MCP tools by simply annotating them.

## Overview

The tools module allows you to:

- Automatically register Kotlin methods as MCP tools using reflection
- Support various parameter types (primitives, collections, optional parameters)
- Handle type conversion between Kotlin types and JSON schema
- Provide error handling for tool execution

## Quick Start

### 1. Annotate Your Methods

```kotlin
class WeatherTools {
  @LLMTool("Get current weather for a city")
  fun getCurrentWeather(city: String): String {
    return "Weather in $city: Sunny, 25°C"
  }

  @LLMTool("Get weather forecast for multiple days")
  fun getWeatherForecast(city: String, days: Int = 5): String {
    return "Weather forecast for $city over $days days: Mostly sunny"
  }

  @LLMTool("Get weather for multiple cities")
  fun getWeatherForCities(cities: List<String>): List<String> {
    return cities.map { "Weather in $it: Sunny, 25°C" }
  }
}
```

### 2. Register Tools with MCP Server

```kotlin
import io.modelcontextprotocol.kotlin.sdk.server.Server
import com.mattbobambrose.mcp_utils.addTools

val server = Server()
val weatherTools = WeatherTools()

// Automatically register all @LLMTool annotated methods
server.addTools(weatherTools)
```

## API Reference

### @LLMTool Annotation

The `@LLMTool` annotation marks methods to be exposed as MCP tools.

```kotlin
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class LLMTool(val description: String)
```

**Parameters:**

- `description`: Human-readable description of what the tool does

### Extension Function: Server.addTools()

```kotlin
fun Server.addTools(toolsObject: Any)
```

Automatically registers all methods annotated with `@LLMTool` from the given object as MCP tools.

**Parameters:**

- `toolsObject`: Instance of a class containing methods annotated with `@LLMTool`

## Supported Types

### Parameter Types

The following parameter types are automatically supported:

| Kotlin Type           | JSON Schema Type | Example            |
|-----------------------|------------------|--------------------|
| `String`              | `"string"`       | `"hello"`          |
| `Int`                 | `"integer"`      | `42`               |
| `Boolean`             | `"boolean"`      | `true`             |
| `Double`              | `"number"`       | `3.14`             |
| `Float`               | `"number"`       | `2.71f`            |
| `List<T>`             | `"array"`        | `["a", "b", "c"]`  |
| `Set<T>`              | `"array"`        | `["x", "y", "z"]`  |
| `Map<String, String>` | `"object"`       | `{"key": "value"}` |

### Optional Parameters

Methods can have optional parameters with default values:

```kotlin
@LLMTool("Search with optional filters")
fun search(query: String, limit: Int = 10, includeMetadata: Boolean = false): String {
  return "Search results for '$query' (limit: $limit, metadata: $includeMetadata)"
}
```

### Return Types

Tools can return various types:

```kotlin
@LLMTool("Get single result")
fun getSingleResult(): String = "result"

@LLMTool("Get multiple results")
fun getMultipleResults(): List<String> = listOf("result1", "result2")

@LLMTool("Get unique results")
fun getUniqueResults(): Set<String> = setOf("unique1", "unique2")
```

## Examples

### Basic Weather Service

```kotlin
class WeatherService {
  @LLMTool("Get current temperature for a city")
  fun getTemperature(city: String): String {
    // In a real implementation, this would call a weather API
    return "Temperature in $city: 22°C"
  }

  @LLMTool("Get weather conditions")
  fun getConditions(city: String): String {
    return "Conditions in $city: Partly cloudy"
  }

  @LLMTool("Check if it's raining")
  fun isRaining(city: String): Boolean {
    return false // Mock implementation
  }
}
```

### File Operations

```kotlin
class FileTools {
  @LLMTool("List files in directory")
  fun listFiles(directory: String): List<String> {
    return File(directory).listFiles()?.map { it.name } ?: emptyList()
  }

  @LLMTool("Read file content")
  fun readFile(filePath: String): String {
    return try {
      File(filePath).readText()
    } catch (e: Exception) {
      "Error reading file: ${e.message}"
    }
  }

  @LLMTool("Check if file exists")
  fun fileExists(filePath: String): Boolean {
    return File(filePath).exists()
  }
}
```

### Advanced Example with Collections

```kotlin
class DataProcessor {
  @LLMTool("Process list of numbers")
  fun processNumbers(numbers: List<Int>, operation: String = "sum"): String {
    return when (operation) {
      "sum" -> "Sum: ${numbers.sum()}"
      "average" -> "Average: ${numbers.average()}"
      "max" -> "Max: ${numbers.maxOrNull()}"
      "min" -> "Min: ${numbers.minOrNull()}"
      else -> "Unknown operation: $operation"
    }
  }

  @LLMTool("Filter unique values")
  fun getUniqueValues(values: List<String>): Set<String> {
    return values.toSet()
  }

  @LLMTool("Process key-value pairs")
  fun processKeyValuePairs(data: Map<String, String>): String {
    return data.entries.joinToString(", ") { "${it.key}: ${it.value}" }
  }
}
```

## Error Handling

The tools module automatically handles exceptions during tool execution:

```kotlin
@LLMTool("Tool that might fail")
fun riskyOperation(input: String): String {
  if (input.isEmpty()) {
    throw IllegalArgumentException("Input cannot be empty")
  }
  return "Success: $input"
}
```

When an exception occurs, the tool will return an error response with the exception message.

## Testing

The module includes comprehensive tests covering:

- Annotation detection
- Parameter type mapping
- Optional parameter handling
- Method invocation
- Error handling
- Complex type conversions

Run tests:

```bash
./gradlew :tools:test
```

## Integration

### With MCP Server

```kotlin
import io.modelcontextprotocol.kotlin.sdk.server.Server
import com.mattbobambrose.mcp_utils.addTools

fun main() {
  val server = Server()

  // Register multiple tool classes
  server.addTools(WeatherTools())
  server.addTools(FileTools())
  server.addTools(DataProcessor())

  // Start your MCP server
  server.start()
}
```

### Building Custom Tools

```kotlin
class CustomTools {
  @LLMTool("Calculate compound interest")
  fun calculateCompoundInterest(
    principal: Double,
    rate: Double,
    years: Int,
    compoundFrequency: Int = 12
  ): String {
    val amount = principal * Math.pow(1 + rate / compoundFrequency, compoundFrequency * years.toDouble())
    return "Compound interest: ${"%.2f".format(amount - principal)}"
  }

  @LLMTool("Generate random numbers")
  fun generateRandomNumbers(count: Int, min: Int = 1, max: Int = 100): List<Int> {
    return (1..count).map { Random.nextInt(min, max + 1) }
  }
}
```

## Best Practices

1. **Descriptive Annotations**: Use clear, descriptive text in `@LLMTool` annotations
2. **Parameter Names**: Use meaningful parameter names as they become part of the tool's API
3. **Error Handling**: Handle potential errors gracefully within your tool methods
4. **Type Safety**: Stick to supported types for parameters and return values
5. **Documentation**: Document complex tools with examples of expected inputs and outputs