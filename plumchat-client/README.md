## ✨ Interactive MCP Client

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-007396?logo=java&logoColor=white" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.3.2-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Spring%20AI-1.0.0-13aa52" alt="Spring AI"/>
  <img src="https://img.shields.io/badge/Protocol-MCP-6E56CF" alt="MCP"/>
  <img src="https://img.shields.io/badge/Release-2.0.0-1f6feb" alt="Release"/>
  <img src="https://img.shields.io/badge/License-Apache%202.0-blue" alt="License"/>
</p>

<p align="center">
  <b>Test and interact with MCP servers directly — no LLM required.</b><br/>
  Built with Spring Boot + Spring AI + WebFlux. Supports STDIO, SSE, and Streamable HTTP transports.
</p>

<p align="center">
<pre>
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃  🚀 Interactive Model Context Protocol (MCP) Client  ┃
┃      Debug • Inspect • Call • Validate • Repeat      ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
</pre>
</p>

---

### 🔗 Table of Contents
- [⚡ Highlights](#-highlights)
- [🧰 Requirements](#-requirements)
- [🚀 Quick Start](#-quick-start)
- [🧩 Transports & Profiles](#-transports--profiles)
- [🔐 SSE Configuration (Auth & No-Auth)](#-sse-configuration-auth--no-auth)
- [🖥️ CLI Usage](#️-cli-usage)
- [🧪 Examples](#-examples)
- [🧯 Troubleshooting](#-troubleshooting)
- [❓ FAQ](#-faq)
- [🗂️ Project Structure](#️-project-structure)
- [🤝 Contributing](#-contributing)
- [📄 License](#-license)
- [🔗 Useful Links](#-useful-links)

---

## ⚡ Highlights
- 🧵 Multiple transports: **STDIO**, **SSE**, **Streamable HTTP**
- 🧪 Built for testing MCP servers without an LLM
- 🔐 First-class support for **JWT Bearer** authentication (works with API gateways / protected servers)
- 🧭 Interactive CLI: list tools, describe tools, invoke tools with JSON
- 🧱 Spring profile-based configuration for clean separation
- 🛡️ Resilient SSE handling with compatibility tweaks for diverse servers

---

## 🧰 Requirements
- Java 21+
- Maven 3.6+
- Node.js (only if testing STDIO servers like npm MCP servers)

Set environment variables as needed:

```bash
# If using Anthropic (optional LLM use in other contexts)
export ANTHROPIC_API_KEY=your-anthropic-api-key

# If testing Brave Search server via STDIO
export BRAVE_API_KEY=your-brave-api-key

# If connecting to a JWT-protected SSE server
export SERVER_JWT=your-jwt-token
```

---

## 🚀 Quick Start
```bash
# Clone
git clone https://github.com/dbbaskette/mcp-client.git
cd mcp-client

# Run (default: stdio profile)
./mcp-client.sh

# Run with SSE
./mcp-client.sh --profile sse

# Rebuild and run (any profile)
./mcp-client.sh --rebuild -p sse
```

Pro tip: add `-v/--verbose` to the script for extra logs during local debugging.

---

## 🧩 Transports & Profiles
- 🔌 **stdio**: process-based servers (great for npm MCP servers)
- 📡 **sse**: Server-Sent Events over HTTP (great for web MCP servers)
- 🌊 **streamable**: HTTP with streaming responses (experimental servers)

Files live under `src/main/resources`:
- `application-stdio.properties`
- `application-sse.properties`
- `application-streamable.properties`

---

## 🔐 SSE Configuration (Auth & No-Auth)
Below are two common SSE setups.

### Authenticated SSE (JWT Bearer)
Some servers authenticate at a base path (e.g., `/mcp`) and stream at `/sse`.

```properties
# src/main/resources/application-sse.properties
spring.ai.mcp.client.sse.connections.secure.url=http://your-host:8585/mcp
spring.ai.mcp.client.sse.connections.secure.sse-endpoint=/sse
spring.ai.mcp.client.sse.connections.secure.headers.Authorization=Bearer ${SERVER_JWT}

# Optional resilience
apring.ai.mcp.client.sse.connections.secure.timeout=60s
spring.ai.mcp.client.sse.connections.secure.connect-timeout=30s
spring.ai.mcp.client.sse.connections.secure.read-timeout=60s
```

### Public SSE (No Auth)
If your server doesn’t require authentication, configure it like this:

```properties
# src/main/resources/application-sse.properties
spring.ai.mcp.client.sse.connections.public.url=http://localhost:8080
spring.ai.mcp.client.sse.connections.public.sse-endpoint=/sse
```

Global application settings (already tuned for clean output and resilience) in `application.properties`:

```properties
spring.application.name=mcp-client
spring.ai.mcp.client.toolcallback.enabled=true
spring.ai.mcp.client.type=SYNC

# Trim noisy logs, keep essentials
logging.level.io.modelcontextprotocol.client=INFO
logging.level.io.modelcontextprotocol.spec=INFO
logging.level.org.springframework.ai.mcp=INFO
logging.level.reactor.core.publisher.Operators=WARN

# Resilient SSE handling
spring.ai.mcp.client.connection.resilient=true
spring.ai.mcp.client.sse.lenient-parsing=true
```

Note: Some SSE servers may emit occasional events with missing `event:` types; the client is configured to ignore benign anomalies.

---

## 🖥️ CLI Usage
Start the client and use the interactive shell:

```text
mcp-client> help
mcp-client> list-tools
mcp-client> describe-tool <toolName>
mcp-client> tool <toolName> {"param":"value"}
mcp-client> status
mcp-client> exit
```

Script options:

```bash
./mcp-client.sh [OPTIONS]
  -p, --profile <stdio|sse|streamable>
  --rebuild                 # clean build before run
  -v, --verbose             # verbose script logging
  -h, --help                # help
```

---

## 🧪 Examples
- List all available tools from connected servers:

```text
mcp-client> list-tools
```

- Describe a tool’s input schema and usage:

```text
mcp-client> describe-tool spring_ai_mcp_client_example_tool
```

- Invoke a tool with JSON parameters:

```text
mcp-client> tool spring_ai_mcp_client_example_tool {"query":"warehouses","limit":5}
```

---

## 🧯 Troubleshooting
- 500 on SSE endpoint: Ensure a valid `Authorization` header (for protected servers) and correct base/endpoint paths.
- Random SSE event anomalies: Client ignores benign `null`/missing-type events by default.
- Timeouts while listing tools: increase timeouts in your SSE connection block.
- Still stuck? Run with `--rebuild` and verify your env vars: `echo $SERVER_JWT`.

---

## ❓ FAQ
- Do I need an LLM?  
  No. This client is purposely LLM-free for server testing and validation.

- Can I add my own servers easily?  
  Yes — add them to the appropriate profile properties file.

- Can I use this for other JWT-protected SSE servers?  
  Absolutely. Set the `Authorization` header and correct base/endpoint paths.

---

## 🗂️ Project Structure
```text
src/main/java/com/baskettecase/mcpclient/
├─ McpClientApplication.java        # Main Spring Boot app
├─ cli/
│  └─ CliRunner.java                # Interactive CLI
└─ config/
   ├─ McpConnectionConfigService.java
   ├─ McpErrorHandlingConfig.java    # SSE event compatibility handling
   ├─ SslConfiguration.java          # SSL relax (opt-in via properties)
   └─ WebClientConfig.java           # Global WebClient customization

src/main/resources/
├─ application.properties            # Common settings
├─ application-stdio.properties      # STDIO profile config
├─ application-sse.properties        # SSE profile config (JWT-ready)
└─ application-streamable.properties # Streamable HTTP profile config
```

---

## 🤝 Contributing
- Fork the repo, create a feature branch, commit, and open a PR  
- Use clear commit messages (e.g., `feat: add X`, `fix: handle Y`)

```bash
git checkout -b feat/my-improvement
# make changes
git commit -m "feat: my improvement"
git push origin feat/my-improvement
```

---

## 📄 License
Licensed under the **Apache 2.0** License. See `LICENSE` for details.

---

## 🔗 Useful Links
- [Spring AI MCP Docs](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html)
- [MCP Specification](https://modelcontextprotocol.github.io/specification/)
- [Spring Boot Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/)
