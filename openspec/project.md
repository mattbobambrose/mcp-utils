# Project Context

## Purpose

MCP Utils is a Kotlin library for working with the Model Context Protocol (MCP). The project provides two main
capabilities:

1. **Automatic Tool Registration**: Simplifies creating MCP servers by using reflection to automatically register Kotlin
   methods as MCP tools via the `@LLMTool` annotation
2. **MCP Client with OpenAI Integration**: Provides an MCP client (MCPShell) that connects to MCP servers via
   Server-Sent Events (SSE) and processes queries using OpenAI's API with automatic tool calling

The goal is to make it easy for developers to expose Kotlin functionality as LLM tools and to interact with MCP servers
programmatically.

## Tech Stack
- **Language**: Kotlin 2.2.0
- **Build Tool**: Gradle with Gradle Wrapper, multi-module setup
- **JVM**: Java 17 toolchain (minimum requirement)
- **MCP SDK**: io.modelcontextprotocol:kotlin-sdk:0.5.0
- **OpenAI Integration**: com.openai:openai-java:2.12.0
- **HTTP/SSE**: Ktor Client
- **Serialization**: Kotlinx Serialization (JSON)
- **Reflection**: Kotlin Reflection (kotlin-reflect)
- **Testing**: Kotlin Test framework
- **Repositories**: Maven Central, JitPack

## Project Conventions

### Code Style
- Standard Kotlin conventions and idioms
- Function names: camelCase
- Class names: PascalCase
- Package structure: `com.mattbobambrose.mcp_utils.<module>`
- Use of extension functions where appropriate (e.g., `Server.integrateTools()`)
- Annotation-driven configuration (`@LLMTool`)
- Constants grouped in private objects for organization (e.g., `TypeNames` object)
- Explicit type handling with when expressions for type mapping
- Comprehensive error handling with try-catch blocks returning structured error results

### Architecture Patterns
- **Multi-module architecture**: Separate modules for distinct concerns (tools, shell, examples)
- **Reflection-based registration**: Uses Kotlin reflection to discover and register annotated methods dynamically
- **Extension function pattern**: Core functionality exposed as extension functions on SDK types (e.g.,
  `Server.integrateTools()`)
- **Type mapping system**: Converts between Kotlin/Java types and JSON Schema types for MCP compatibility
- **SSE communication**: Shell module uses Server-Sent Events for real-time communication with MCP servers
- **Tool calling workflow**: Integrates OpenAI's function calling with MCP tool execution
- **Convention plugins**: Build configuration managed through custom Gradle convention plugins in buildSrc/

### Testing Strategy
- **Framework**: Kotlin Test (`kotlin.test`)
- **Test location**: Each module has tests in `src/test/kotlin/`
- **Coverage approach**:
  - Unit tests for all annotated method detection
  - Parameter type detection tests for all supported types (String, Int, Boolean, Double, Float, List, Set, Map)
  - Optional parameter handling tests
  - Method invocation tests
  - Return type tests (including collections)
  - Exception handling tests
  - Type conversion mapping tests
- **Run tests**: `./gradlew test` (all modules) or `./gradlew :<module>:test` (specific module)
- **Assertions**: Use kotlin.test assertions (assertEquals, assertTrue, assertFalse, assertNotNull)

### Git Workflow
- **Main branch**: `master`
- **Current version**: 1.0.0
- Standard feature branch workflow recommended
- Build verification: `./gradlew check` before commits
- Uses JitPack for distribution (jitpack.yml present)

## Domain Context

### Model Context Protocol (MCP)
MCP is a protocol that allows Large Language Models to interact with external tools and data sources. It defines:
- Tools with JSON Schema input specifications
- Tool execution requests and responses
- Server-client communication patterns

### LLM Tool Registration
The `@LLMTool` annotation marks methods to be exposed as MCP tools:
- Annotation contains the tool description for the LLM
- Method parameters are automatically converted to JSON Schema
- Return values are converted to MCP response format (TextContent)
- Supports optional parameters with default values

### Supported Parameter Types
- Primitives: String, Int, Boolean, Double, Float (and their boxed Java equivalents)
- Collections: List, Set (converted to JSON array)
- Maps: Map (converted to JSON object)
- Unknown types default to string

### OpenAI Integration

The shell module converts MCP tools to OpenAI function format, allowing the OpenAI API to discover and call tools during
conversation processing.

## Important Constraints

### Technical Requirements
- **Minimum Java version**: Java 17 (enforced by toolchain)
- **Kotlin version**: 2.2.0
- **Reflection requirement**: Tools module requires kotlin-reflect for annotation processing
- **Type limitations**: Only specific primitive types and collections are supported for auto-registration
- **Map handling**: Map parameter handling has TODO for testing (line 133 of MCPTools.kt)

### Runtime Requirements
- **OpenAI API Key**: Shell module requires `OPENAI_API_KEY` environment variable
- **Optional**: `OPENAI_ORG_ID` and `OPENAI_PROJECT_ID` environment variables
- **MCP Server**: Shell module requires a running MCP server accessible via HTTP/SSE

### Build Configuration
- Build cache and configuration cache enabled
- Uses version catalog for dependency management (implied by Gradle structure)
- Multi-module build with module interdependencies

## External Dependencies

### Required Services
- **MCP Servers**: Shell module connects to external MCP servers via SSE (typically http://localhost:8080/ in examples)
- **OpenAI API**: Shell module makes API calls to OpenAI for query processing and tool calling

### Key Libraries
- **MCP Kotlin SDK** (io.modelcontextprotocol:kotlin-sdk): Core protocol implementation
- **OpenAI Java** (com.openai:openai-java): OpenAI API client
- **Ktor Client**: HTTP client for SSE connections to MCP servers
- **Kotlinx Serialization**: JSON parsing and serialization for MCP messages
- **Kotlin Reflection**: Runtime reflection for discovering @LLMTool annotations

### Distribution
- Published via JitPack (jitpack.yml configuration present)
- Accessible as a library dependency for other Kotlin/JVM projects
