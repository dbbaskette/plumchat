# Gotchas

Record edge cases, warnings, and prevention strategies discovered during development.

- Sensitive data handling: ensure redaction on logs and traces for auth and SQL.
- Approval flow: never execute SQL/admin actions without explicit UI approval.
- CORS: avoid `*`; misconfig leads to subtle security issues in browsers.
- SSH hardening: restrict user commands; rotate keys; audit access.
- Key path validation: only load private keys from `plumchat.mgmt.allowed-key-dirs`; reject others to avoid path traversal/secret exfiltration.
- Timeouts: set JDBC and SSH command timeouts to prevent hangs.
