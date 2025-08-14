# Implementation Details

This document records technical decisions and their rationale.

## Versions
- Java 21, Spring Boot 3.5.4, Spring AI 1.0.0, Lombok 1.18.34, PostgreSQL JDBC 42.7.4, Testcontainers 1.20.1.

## MCP Transport
- Default: SSE (HTTP) between Host and Servers; optional stdio for local tools.

## Authentication
- UI submits creds to Host over HTTPS.
- Host forwards creds to MCP Query Server `/auth/login`; servers validate via short-lived JDBC connection.
- Servers issue short-lived JWTs; Host stores only server tokens, not DB creds.
- DB list retrieval via `pg_database` with privilege checks.

## Client-to-Host Security Measures
- TLS 1.3, HSTS, strict CSP, no mixed content.
- POST-only login, no query-params, `Cache-Control: no-store` on auth responses.
- Cookie mode: `HttpOnly`, `Secure`, `SameSite=Strict`, CSRF token.
- Bearer mode: in-memory storage, rotation; avoid `localStorage`.
- CORS allow-list and restricted methods/headers.
- Redaction in logs; limited error detail for auth routes.

## Management (gpstart/gpstop)
- SSH-based adapter from Mgmt Server to GP master/coordinator.
- Dedicated restricted user, keypair management, host allow-list.
- Feature-flagged and disabled by default.
 - Guardrails: allow-list for hosts/users; optional key loading only from `plumchat.mgmt.allowed-key-dirs`.
 - Endpoints/tools: `gpstart`, `gpstop`, `gpstate`; variants accept in-body key or server-side key path (validated).

## Observability
- Spring Boot Actuator, structured logs, correlation IDs.
