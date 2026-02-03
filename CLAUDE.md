<!-- OPENSPEC:START -->

# OpenSpec Instructions

These instructions are for AI assistants working in this project.

Always open `@/openspec/AGENTS.md` when the request:

- Mentions planning or proposals (words like proposal, spec, change, plan)
- Introduces new capabilities, breaking changes, architecture shifts, or big performance/security work
- Sounds ambiguous and you need the authoritative spec before coding

Use `@/openspec/AGENTS.md` to learn:

- How to create and apply change proposals
- Spec format and conventions
- Project structure and guidelines

Keep this managed block so 'openspec update' can refresh the instructions.

<!-- OPENSPEC:END -->

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Kotlin multi-module project that provides utilities for working with the Model Context Protocol (MCP). The
project consists of two main modules:

- **tools**: Contains the `AddTools` utility for automatically registering Kotlin methods as MCP tools via reflection
- **shell**: Contains the `MCPShell` client for connecting to MCP servers and processing queries using OpenAI's API

## Build Commands

This project uses Gradle with the Gradle Wrapper. All commands should be run from the project root:

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run specific tests
./gradlew :tools:test
./gradlew :shell:test

# Clean build artifacts
./gradlew clean

# Check for dependency updates
./gradlew dependencyUpdates

# Run all checks (tests, linting, etc.)
./gradlew check
```

## Architecture

### Tools Module (`tools/`)

- **AddTools.kt**: Extension function for MCP Server that uses reflection to automatically register Kotlin methods as
  MCP tools
- **@LLMTool annotation**: Marks methods to be exposed as MCP tools with descriptions
- **Type mapping**: Converts Kotlin/Java types to JSON schema types for MCP compatibility
- **Parameter handling**: Supports primitive types, collections (List, Set, Map), and optional parameters

### Shell Module (`shell/`)

- **MCPShell.kt**: MCP client that connects to servers via Server-Sent Events (SSE)
- **OpenAI integration**: Uses OpenAI's API to process queries and call MCP tools
- **Tool execution**: Converts MCP tools to OpenAI function format and handles tool calling workflow

## Key Dependencies

- **MCP Kotlin SDK**: `io.modelcontextprotocol:kotlin-sdk:0.5.0`
- **OpenAI Java**: `com.openai:openai-java:2.12.0`
- **Ktor Client**: For HTTP/SSE communication
- **Kotlinx Serialization**: For JSON handling
- **Kotlin Reflection**: For automatic tool registration

## Development Notes

### Adding New Tools

1. Create methods in a class and annotate with `@LLMTool("description")`
2. Use the `Server.addTools(toolsObject)` extension function to register them
3. Supported parameter types: String, Int, Boolean, Double, Float, List, Set, Map
4. Optional parameters are supported with default values

### Testing

- Tests are located in `tools/src/test/kotlin/`
- Use `./gradlew test` to run all tests
- The project uses Kotlin Test framework

### Configuration

- Java toolchain version: 17
- Kotlin version: 2.2.0
- Build cache and configuration cache are enabled
- Uses JitPack repository for some dependencies