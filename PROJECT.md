# Project Instructions: PlumChat

> **Instructions for the User**:

You are a master SpringBoot developer and also an expert at Greenplum. Leverage your unique and powerful problem-solving skills to build this app decribed below.  You should think and plan before doing, and output a markdown doc with a detailed plan with phases and steps.  It should have a paragraph decription of what will be done at the start of each phase.  It should also detail what libraries, frameworks are going to be used for that set of work

---

## 1. Project Overview & Goal

* **What is the primary goal of this project?**
    * Using https://github.com/dbbaskette/imc-chatbot as a basis, we will build an intelligent chatbot, **PlumChat**, that employs an agentic workflow based on the **Model Context Protocol (MCP)**. The application will feature a central AI agent (the MCP Host) that leverages specialized tools and data sources (MCP Servers) to allow users to interact with their Greenplum databases. Users will be able to explore database schemas, perform administrative tasks (like starting/stopping the database), and query the data warehouse using natural language. The system will translate natural language questions into complex SQL, display it for user approval, and then execute it. User login will be authenticated against Greenplum, granting them access to a list of their available databases to work with. Put a placeholder in the readme and in the project for a project log to be used at the top of the readme. (to be provides later)

* **Who are the end-users?**
    * Greenplum Administrators and Greenplum End-Users

## 2. Tech Stack

* **Language(s) & Version(s)**: Java 21
* **Framework(s)**: Spring Boot 3.5.4
* **Key Libraries**:
    * **Spring AI 1.0.0**: For the core AI/LLM integration and agentic logic within the MCP Host.
    * **Spring WebFlux**: For building reactive APIs, especially for the MCP servers.
    * **JDBC Driver for Greenplum**: For all database interactions.
    * **Lombok**: To reduce boilerplate code.
    * **React / Angular / Vue**: For the frontend Chatbot UI.
* **Build/Package Manager**: Maven
* **Communication Protocol**: MCP over SSE (default) between Host and Servers; optional stdio for local tools. UI communicates with Host over HTTP.

## 3. Architecture & Design

* **High-Level Architecture**: The application will be built using a **Model Context Protocol (MCP)** architecture. This creates a clear separation between the central AI agent (Host) and the specialized components that provide data and tools (Servers). This modular, microservices-based approach enhances scalability and maintainability. For this demonstration-scale project, the frontend UI will connect directly to the PlumChat Host.

* **Key Components (MCP)**:
    * **PlumChat Host (The AI Brain)**: A Spring Boot application using Spring AI that acts as the central coordinator and the direct entry point for the UI. It receives user prompts, determines the necessary context or tools, and makes requests to the appropriate MCP servers. It is responsible for orchestrating the entire agentic workflow and managing **CORS** configuration for the UI.
    * **Greenplum Schema Server (MCP)**: A dedicated microservice that acts as an MCP Server. Its role is to provide database schema information (tables, columns, views) as a *Resource* to the Host.
    * **Greenplum Query Server (MCP)**: An MCP Server that exposes SQL query execution as a *Tool*. It receives SQL from the Host (after user approval), runs it securely against the database, and returns the results.
    * **Greenplum Management Server (MCP)**: An MCP Server that exposes administrative functions (e.g., `gpstart`, `gpstop`) as *Tools*. The Host can invoke these tools based on user commands, subject to approval.
    * We can use https://github.com/dbbaskette/mcp-server as the basis for each of the MCP Servers. It is a generic MCP server that provides SSE and STDIO support.  

* **Data Flow**: The Host sends user prompts to the AI agent, which in turn sends them to the MCP servers.

* **Directory Structure**: The project will be organized as a multi-module Maven project to reflect the MCP architecture.
    * `plumchat/`: Root project directory.
    * `plumchat/host/`: The main PlumChat Host application (Spring AI).
    * `plumchat/mcp-schema-server/`: The Greenplum Schema Server module.
    * `plumchat/mcp-query-server/`: The Greenplum Query Server module.
    * `plumchat/mcp-mgmt-server/`: The Greenplum Management Server module.
    * `plumchat/ui/`: The frontend chatbot interface.

## 4. Coding Standards & Conventions

* **Code Style**: Google Java Style Guide.
* **Naming Conventions**:
    * Use `camelCase` for variables and methods.
    * Services should be suffixed with `Service` (e.g., `AuthenticationService`).
    * MCP Servers should be clearly named for their function (e.g., `SchemaProviderService`).
* **API Design**:
    * RESTful APIs following standard HTTP verbs, or gRPC for high-performance inter-service communication.
    * Define clear and versioned schemas (e.g., using OpenAPI or Protocol Buffers) for requests and responses between the Host and MCP Servers. This is critical for the MCP contract.
* **Error Handling**: Use custom exception classes and return standardized JSON error responses from all services.

## 5. Important "Do's and Don'ts"

* **DO**: Write unit tests for all new business logic, especially for the tool implementations in the MCP servers.
* **DO**: Implement explicit user approval steps in the PlumChat Host before executing any database-altering commands or queries sent to the MCP servers.
* **DO**: Log important events and errors across all services for traceability.
* **DO**: Configure CORS correctly in the PlumChat Host to allow the frontend application to connect.
* **DON'T**: Do not commit secrets or API keys directly into the repository. Use Spring Boot's externalized configuration mechanisms.
* **DON'T**: Do not allow the MCP Host to have direct database credentials. All database interaction must be delegated to the specialized and secured MCP servers.

---

## 6. Phased Delivery Plan & Checklist

Below is the phased plan with checkboxes to track progress as we implement PlumChat. We will check these off as tasks are completed.

### Phase 0: Decisions and Environment

- **Decisions**
  - [x] Confirm and pin versions; create `versions.txt` (Java 21, Spring Boot 3.5.4, Spring AI 1.0.1, Lombok, Greenplum JDBC)
  - [x] Choose frontend framework: React + Vite + TypeScript + Tailwind + shadcn/ui
  - [x] Select MCP transport: MCP over SSE (default), optional stdio for local tools
  - [x] Confirm authentication flow (Greenplum JDBC credential check + database list retrieval)
  - [x] Confirm `gpstart`/`gpstop` approach: Remote SSH adapter to GP master/coordinator (no install required on GP host)
  - [ ] Confirm authentication flow (Greenplum JDBC credential check + database list retrieval)
  - [ ] Confirm `gpstart`/`gpstop` approach (local shell vs remote RPC) and environment constraints
- **Artifacts**
  - [ ] Initialize Git repository (confirm if this project should be a Git repo)
  - [ ] Add Maven Wrapper at root
  - [x] Create documentation files: `mental_model.md`, `implementation_details.md`, `gotchas.md`, `quick_reference.md`
  - [x] Add a placeholder section for a Project Log at the top of `README.md`

Libraries/frameworks: Maven + Wrapper, Git, Java 17 toolchain

Reference Docs:
    https://docs.spring.io/spring-ai/reference/index.html
    https://docs.spring.io/spring-ai/reference/api/chatclient.html
    https://docs.spring.io/spring-ai/reference/api/advisors.html
    https://docs.spring.io/spring-ai/reference/api/prompt.html
    https://docs.spring.io/spring-ai/reference/api/structured-output-converter.html
    https://docs.spring.io/spring-ai/reference/api/tools.html
    https://docs.spring.io/spring-ai/reference/api/mcp/mcp-overview.html
    https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html
    https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html
    https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html
    https://docs.spring.io/spring-ai/reference/api/etl-pipeline.html
    https://docs.spring.io/spring-ai/reference/api/vectordbs/pgvector.html

### Phase 1: Project Scaffolding (Multi-Module Maven)

- [ ] Create parent POM with dependencyManagement sourced from `versions.txt`
- [ ] Create modules: `host/`, `mcp-schema-server/`, `mcp-query-server/`, `mcp-mgmt-server/`, `ui/`
- [ ] Root build succeeds with `mvn clean package`

Libraries/frameworks: Spring Boot 3.5.4 parent, BOM-managed dependencies

### Phase 2: PlumChat Host (AI Brain)

- [ ] Chat endpoint(s) to receive prompts and orchestrate MCP calls
- [ ] Explicit approval workflow before executing SQL or admin actions
- [ ] MCP client adapters for Schema, Query, and Mgmt servers
- [ ] CORS configuration for UI access
- [ ] Centralized error model and structured logging
- [ ] OpenAPI spec for Host APIs
- [ ] Externalized configuration in `application.yml`

Libraries/frameworks: Spring AI 1.0.0, Spring WebFlux, Spring Boot Actuator, Lombok

### Phase 3: MCP Servers

- **Schema Server**
  - [ ] Expose schema discovery (tables, columns, views) as an MCP Resource with pagination/filtering
- **Query Server**
  - [ ] Execute SQL provided by Host post-approval; enforce timeouts, fetch size, and result pagination
  - [ ] Redact sensitive data in logs
- **Management Server**
  - [ ] Implement tools: `gpstart`, `gpstop`, and status via remote SSH to GP master/coordinator
  - [ ] Key management and hardening (SSH keypair, restricted user, host allow-list)
  - [ ] Role checks/guardrails and feature flag (disabled by default)
- **Quality**
  - [ ] Unit tests for server tool implementations

Libraries/frameworks: Spring WebFlux, JDBC (Greenplum/PostgreSQL driver), Lombok

### Phase 4: Authentication Against Greenplum

- [ ] Login endpoint validates user credentials via short-lived JDBC connection
- [ ] Establish session/token on success (no persistence of DB credentials in Host)
- [ ] Retrieve and return list of accessible databases
- [ ] Security configuration hardened and externalized

Libraries/frameworks: Spring Security, JDBC (HikariCP)

### Phase 5: Frontend UI (Chat)

- [ ] Scaffold chosen framework in `ui/`
- [ ] Implement login form and session handling
- [ ] Chat interface with SQL preview and Approve/Cancel flow
- [ ] Results rendering (tabular) and error states
- [ ] Environment-driven API base URL; local dev CORS verified

Libraries/frameworks: React (Vite + TypeScript) or Angular/Vue alternative; optional UI toolkit (e.g., MUI)

### Phase 6: Observability, Errors, and Compliance

- [ ] Enable Actuator endpoints across services
- [ ] Structured, contextual logs with correlation IDs where applicable
- [ ] Consistent JSON error schema across Host and Servers; documented
- [ ] Document edge cases and approvals in `gotchas.md`

Libraries/frameworks: Spring Boot Actuator, Micrometer

### Phase 7: Tests, Packaging, and DevOps

- [ ] Unit tests for Host orchestration logic
- [ ] Integration tests for MCP server tools (Testcontainers or mocks)
- [ ] Dockerfiles for each module
- [ ] Run scripts and `.env.example` for local dev
- [ ] Basic CI pipeline (build + test)

Libraries/frameworks: JUnit 5, Mockito, Testcontainers (as feasible)
