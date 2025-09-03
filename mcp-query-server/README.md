# Spring AI MCP Server Foundation

A comprehensive Model Context Protocol (MCP) server built with Spring AI and Spring Boot, providing a solid foundation for building MCP-enabled applications. This server supports both STDIO transport (for Claude Desktop integration) and SSE transport (for web-based clients).

For more information, see the [MCP Server Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html) reference documentation.

## Overview

This MCP server demonstrates:
- Integration with `spring-ai-mcp-server-webflux-spring-boot-starter`
- Dual transport support: STDIO and SSE (Server-Sent Events)
- Automatic tool registration using Spring AI's `@Tool` annotation
- Clean separation of concerns with dedicated `ToolsService`
- Production-ready logging configuration
- Comprehensive test coverage with 27+ unit tests

## Available Tools

### 🔤 capitalizeText
- **Description**: Capitalize the first letter of each word in the input text
- **Parameters**: 
  - `text` (String): Input text to capitalize
- **Example**: `"hello world"` → `"Hello World"`

### 🧮 calculate
- **Description**: Perform basic mathematical operations
- **Parameters**:
  - `number1` (double): First number
  - `number2` (double): Second number
  - `operator` (String): Mathematical operator (+, -, *, /, %, ^)
- **Example**: `calculate(15, 3, "+")` → `18.0`

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.6+

### Building the Project
```bash
./mvnw clean install
```

### Running the Server

#### For Claude Desktop (STDIO Mode)
```bash
java -Dspring.profiles.active=stdio -jar target/mcp-server-0.0.1-SNAPSHOT.jar
```

#### For Web Clients (SSE Mode - Default)
```bash
java -jar target/mcp-server-0.0.1-SNAPSHOT.jar
```
Server will be available at `http://localhost:8080/mcp/message`

### Testing the Server
Use the included test script:
```bash
# Test STDIO mode
./test-mcp.sh --stdio --test-tools

# Test SSE mode  
./test-mcp.sh --sse --test-tools

# Build and test both modes
./test-mcp.sh --build --both --test-tools
```

## Claude Desktop Integration

### 1. Add to Claude Desktop Configuration

Add this to your Claude Desktop `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "mcp-server": {
      "command": "java",
      "args": [
        "-Dspring.profiles.active=stdio",
        "-jar",
        "/absolute/path/to/mcp-server/target/mcp-server-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

### 2. Restart Claude Desktop

The tools `capitalizeText` and `calculate` will be available for use.

### 3. Test the Tools

Try asking Claude Desktop:
- "Can you capitalize this text: 'hello world from mcp server'"
- "Calculate 15 + 3 using the calculator tool"

## Configuration

The server uses profile-based configuration:

### STDIO Profile (`application-stdio.properties`)
```properties
# STDIO Transport for Claude Desktop
spring.ai.mcp.server.stdio=true
spring.main.web-application-type=none
spring.main.banner-mode=off

# Logging to file only (console interferes with MCP protocol)
logging.level.root=OFF
logging.config=classpath:logback-stdio.xml
```

### SSE Profile (`application-sse.properties`)
```properties
# SSE Transport for Web Clients
spring.ai.mcp.server.stdio=false
server.port=8080

# MCP endpoint
spring.ai.mcp.server.sse-message-endpoint=/mcp/message
```

## Architecture

### Core Components

- **`McpServerApplication`**: Main Spring Boot application with tool registration
- **`ToolsService`**: Service containing MCP tools with `@Tool` annotations
- **Transport Layer**: Automatic Spring AI MCP transport configuration
- **Configuration**: Profile-based setup for different deployment modes

### Project Structure
```
src/
├── main/java/com/baskettecase/mcpserver/
│   ├── McpServerApplication.java      # Main application
│   └── ToolsService.java              # MCP tools implementation
├── main/resources/
│   ├── application.properties         # Base configuration
│   ├── application-stdio.properties   # STDIO transport config
│   ├── application-sse.properties     # SSE transport config
│   └── logback-stdio.xml              # STDIO logging config
└── test/java/
    ├── ToolsServiceTest.java          # Comprehensive tool tests
    └── org/springframework/ai/mcp/sample/client/
        ├── SampleClient.java          # Example MCP client
        ├── ClientStdio.java           # STDIO client example
        └── ClientSse.java             # SSE client example
```

## Development

### Adding New Tools

Add methods to `ToolsService` with the `@Tool` annotation:

```java
@Tool(description = "Your tool description")
public String yourTool(String parameter) {
    // Your implementation
    return "result";
}
```

### Running Tests
```bash
./mvnw test
```

### Viewing Logs

- **STDIO mode**: Logs go to `/tmp/mcp-server-stdio.log`
- **SSE mode**: Logs go to console and `./target/mcp-server-sse.log`

## Client Examples

### Manual MCP Client (STDIO)
```java
var stdioParams = ServerParameters.builder("java")
    .args("-Dspring.profiles.active=stdio", "-jar", "target/mcp-server-0.0.1-SNAPSHOT.jar")
    .build();

var transport = new StdioClientTransport(stdioParams);
var client = McpClient.sync(transport).build();
```

### Manual MCP Client (SSE)
```java
var transport = new WebFluxSseClientTransport(
    WebClient.builder().baseUrl("http://localhost:8080"));
var client = McpClient.sync(transport).build();
```

## Dependencies

Key dependencies include:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-mcp-server-webflux-spring-boot-starter</artifactId>
    <version>1.1.0-SNAPSHOT</version>
</dependency>
```

This starter provides:
- Reactive and STDIO transport support
- Auto-configured MCP endpoints
- Tool callback registration
- Spring WebFlux integration

## Contributing

This project serves as a foundation for MCP server development. Feel free to:
- Add new tools to `ToolsService`
- Extend transport configurations
- Improve test coverage
- Add new MCP capabilities

## Additional Resources

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [MCP Server Boot Starter Docs](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)
- [Model Context Protocol Specification](https://modelcontextprotocol.github.io/specification/)
- [Claude Desktop MCP Guide](https://claude.ai/mcp)

## License

This project is provided as-is for educational and development purposes.