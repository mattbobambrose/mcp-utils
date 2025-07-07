# MCP Utils

A Kotlin library for working with the Model Context Protocol (MCP), providing utilities for both creating MCP servers
with automatic tool registration and connecting to MCP servers as a client.

## Overview

This project consists of two main modules:

- **tools**: Automatic tool registration for MCP servers using Kotlin reflection
- **shell**: MCP client for connecting to servers and processing queries with OpenAI integration

## Quick Start

### Prerequisites

- Java 17 or higher
- Gradle (wrapper included)
- OpenAI API key (for shell module)

### Building

```bash
# Build the entire project
./gradlew build

# Run tests
./gradlew test

# Clean build artifacts
./gradlew clean
```

### Environment Setup

For the shell module, you'll need to set up your OpenAI API key:

```bash
export OPENAI_API_KEY="your-api-key-here"
# Optional:
export OPENAI_ORG_ID="your-org-id"
export OPENAI_PROJECT_ID="your-project-id"
```

## Modules

### Tools Module

The tools module provides automatic tool registration for MCP servers using Kotlin reflection. Simply annotate your
methods with `@LLMTool` and they'll be automatically exposed as MCP tools.

**Example:**

```kotlin
class MyTools {
  @LLMTool("Get the current weather for a city")
  fun getWeather(city: String): String {
    return "Weather in $city: Sunny, 25°C"
  }
}

// Register tools with MCP server
server.addTools(MyTools())
```

### Shell Module

The shell module provides an MCP client that can connect to MCP servers and process queries using OpenAI's API with
automatic tool calling.

**Example:**

```kotlin
MCPShell().use { shell ->
  shell.connectToServer("http://localhost:8080/")
  val response = shell.processQuery("What's the weather like in San Francisco?")
  println(response)
}
```

## Dependencies

- **MCP Kotlin SDK**: Model Context Protocol implementation
- **OpenAI Java**: OpenAI API client
- **Ktor**: HTTP client for SSE communication
- **Kotlinx Serialization**: JSON handling
- **Kotlin Reflection**: Automatic tool registration

## Development

### Project Structure

```
mcp-utils/
├── tools/          # Tool registration utilities
├── shell/          # MCP client implementation
├── buildSrc/       # Gradle convention plugins
└── gradle/         # Dependency version catalog
```

### Build System

This project uses Gradle with:

- Multi-module setup
- Version catalog for dependency management
- Build and configuration caching enabled
- Java 17 toolchain

### Testing

```bash
# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :tools:test
./gradlew :shell:test
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Run `./gradlew check` to ensure all tests pass
6. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.