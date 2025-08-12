# PlumChat Mental Model

This document captures the conceptual architecture and principles guiding PlumChat.

## Architecture Overview
- Central **MCP Host** (Spring Boot + Spring AI) orchestrates agentic workflows.
- Specialized **MCP Servers** (Schema, Query, Mgmt) expose data and tools via MCP over SSE; optional stdio for local tools.
- **UI** connects to Host over HTTP and participates in approval flows.

## Key Principles
- Host does not hold DB creds; delegates to servers with short-lived tokens.
- Explicit user approval before executing SQL or admin actions.
- Clear contracts (DTOs/OpenAPI) and consistent error model.
- Observability by default (structured logs, health, metrics).

## Data Flow (High-Level)
1. User sends prompt → Host (AI/agent orchestrates)
2. Host gathers context via MCP servers (schema/resources)
3. Host proposes SQL/admin action → UI approval
4. On approval, Host invokes server tool; returns results to UI

## Security Model
- HTTPS everywhere; CORS allow-list to UI origin(s).
- Redaction/no persistence of sensitive data; short-lived tokens.
- SSH-based mgmt tools to GP master with restricted accounts.

## Evolution
- Start minimal with REST-like DTOs over MCP SSE; iterate toward richer tools/resources.
- Swap drivers/infra behind stable abstractions.
