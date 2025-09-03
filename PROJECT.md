# Project Instructions: PlumChat

> **Project Log:** A project log will be maintained at the top of the `README.md` file to track notable changes and versions.

> **Instructions**:

You are a master SpringBoot developer and also an expert at Greenplum. Leverage your unique and powerful problem-solving skills to build this app decribed below.  You should think and plan before doing, and output a markdown doc with a detailed plan with phases and steps.  It should have a paragraph decription of what will be done at the start of each phase.  It should also detail what libraries, frameworks are going to be used for that set of work.   The format is at the bottom of this doc.  Fill it in there.

---

## 1. Project Overview & Goal

* **What is the primary goal of this project?**
     **PlumChat** is an agentic application that leverages a chat interface and several MCP tools to provide a natural language interface to a Greenplum(postgrs) database.  The application will feature a central AI agent (the MCP Client) that leverages specialized tools and data sources (MCP Servers) and a chat interface to allow users to interact with their Greenplum databases. Users will be able to explore database schemas, perform administrative tasks (like starting/stopping the database), and query the data warehouse using natural language. The system will translate natural language questions into complex SQL, display it for user approval, and then execute it. User login will be authenticated against Greenplum, granting them access to a list of their available databases to work with. Put a placeholder in the readme and in the project for a project log to be used at the top of the readme. (to be provides later)>. This project is broken into several pieces to make implmentation more modular.
     We have starting code
        * mcp-client - This is a generic MCP client that has a script for starting and can be used to test our STDIO and SSE servers. We should keep mcp-client as is because it will be our initial testing tool. mcp-db-client is anopther clone of the same repo and will be the basis for the MCP clinet portion of the application.
        * mcp-server - This is a generic MCP Server.  I have cloned it into mcp-mgmt-server, mcp-query-server, and mcp-schema-server.  It will form the basis of each of those because it is verified and works.  We will start with mcp-schema-server and work it to completion and then move to query and end with management.



* **Who are the end-users?**
    * Greenplum Administrators and Greenplum End-Users

## 2. Tech Stack

* **Language(s) & Version(s)**: Java 21, Spring AI 1.0.1
* **Framework(s)**: Spring Boot 3.5.5
* **Key Libraries**:
    * **Spring AI 1.0.0**: For the core AI/LLM integration and agentic logic within the MCP Host.
    * **Spring WebFlux**: For building reactive APIs, especially for the MCP servers.
    * **JDBC Driver for Greenplum**: For all database interactions.
    * **Lombok**: To reduce boilerplate code.
    * **React**: For the frontend Chatbot UI.
* **Build/Package Manager**: Maven, git (create a repo and use https://github.com/dbbaskette/plumchat as the remote)
* **Communication Protocol**: MCP over SSE (default) between Host and Servers; optional stdio for local tools. UI communicates with Host over HTTP.
* **.env / .env.example** This is how we provide things that are secret. .env should be ignored from git.

## 3. Architecture & Design

* **High-Level Architecture**: The application will be built using a **Model Context Protocol (MCP)** architecture. This creates a clear separation between the central AI agent (Client) and the specialized components that provide data and tools (Servers). This modular, microservices-based approach enhances scalability and maintainability. For this demonstration-scale project, the frontend UI will connect directly to the PlumChat Host.

**Docs to follow:  (MAKE SURE ALL CODE ALIGNS WITH THESE GUIDES)**
    * https://docs.spring.io/spring-ai/reference/api/chatclient.html
    * https://docs.spring.io/spring-ai/reference/api/tools.html
    * https://docs.spring.io/spring-ai/reference/api/mcp/mcp-overview.html
    * https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html
    * https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html
    * https://docs.spring.io/spring-ai/reference/api/mcp/mcp-helpers.html

* **Key Components (MCP)**:
    * **PlumChat Client (The AI Brain)**: A Spring Boot application (`plumchat-client`) using Spring AI that acts as the central coordinator and the direct entry point for the UI. It receives user prompts via REST APIs, determines the necessary context or tools, and makes requests to the appropriate MCP servers. It orchestrates the entire agentic workflow and serves the React UI as static resources.
    * **PlumChat UI**: A React single-page application (`plumchat-ui`) that provides the chat interface. Built separately and integrated into `plumchat-client` static resources following Spring Boot best practices.
    * **MCP Schema Server**: A dedicated microservice that acts as an MCP Server. Its role is to provide database schema information (tables, columns, views) to plumchat-client.
    * **MCP Query Server**: An MCP Server that exposes SQL query execution as a *Tool*. It receives SQL from plumchat-client (after user approval), runs it securely against the database, and returns the results.
    * **MCP Management Server**: An MCP Server that exposes administrative functions (e.g., `gpstart`, `gpstop`) as *Tools*. plumchat-client can invoke these tools based on user commands, subject to approval.

* **Authentication**: The `plumchat-client` will handle user authentication. The flow will be as follows:
    1. The user enters their Greenplum credentials in the React UI.
    2. The credentials are securely sent to the `plumchat-client` REST API.
    3. The `plumchat-client` uses Spring Security to authenticate the user against the Greenplum database.
    4. Upon successful authentication, the `plumchat-client` will generate a JWT (JSON Web Token) that will be used for session management for all subsequent requests from the UI.
    5. The `plumchat-client` will then interact with the MCP servers on behalf of the authenticated user.

* **MCP Server Implementation Details**:
    * **`mcp-schema-server`**: Will use the JDBC metadata APIs to retrieve schema information (tables, columns, views, etc.).
    * **`mcp-query-server`**: Will handle SQL queries asynchronously to avoid blocking long-running queries. It will only execute queries that have been explicitly approved by the user in the UI.
    * **`mcp-mgmt-server`**: Will execute administrative commands like `gpstart` and `gpstop`. This will be done with caution, and the server will run with the minimum required privileges to perform these tasks.

* **Data Flow**: The Host sends user prompts to the AI agent, which in turn sends them to the MCP servers.

* **Directory Structure**: The project will be organized as a multi-module Maven project to reflect the MCP architecture.
    * `plumchat/`: Root project directory with parent POM.
    * `plumchat/mcp-client/`: Generic MCP testing client (stable, for testing servers).
    * `plumchat/plumchat-client/`: The main PlumChat Host application (Spring AI + REST APIs).
    * `plumchat/plumchat-ui/`: React frontend module (builds into plumchat-client static resources).
    * `plumchat/mcp-schema-server/`: The Greenplum Schema Server module.
    * `plumchat/mcp-query-server/`: The Greenplum Query Server module.
    * `plumchat/mcp-mgmt-server/`: The Greenplum Management Server module.

## 4. Coding Standards & Conventions

* **Code Style**: Google Java Style Guide.
* **Package Names** com.baskettecase.plumchat...
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
* **DON'T**: Do not allow the PlumChat Client to have direct database credentials. All database interaction must be delegated to the specialized and secured MCP servers.

---
description: Ensure what you implement Always Works™ with comprehensive testing
---

# How to ensure Always Works™ implementation

Please ensure your implementation Always Works™ for: $ARGUMENTS.

Follow this systematic approach:

## Core Philosophy

- "Should work" ≠ "does work" - Pattern matching isn't enough
- I'm not paid to write code, I'm paid to solve problems
- Untested code is just a guess, not a solution

# The 30-Second Reality Check - Must answer YES to ALL:

- Did I run/build the code?
- Did I trigger the exact feature I changed?
- Did I see the expected result with my own observation (including GUI)?
- Did I check for error messages?
- Would I bet $100 this works?

# Phrases to Avoid:

- "This should work now"
- "I've fixed the issue" (especially 2nd+ time)
- "Try it now" (without trying it myself)
- "The logic is correct so..."

# Specific Test Requirements:

- UI Changes: Actually click the button/link/form
- API Changes: Make the actual API call
- Data Changes: Query the database
- Logic Changes: Run the specific scenario
- Config Changes: Restart and verify it loads

# The Embarrassment Test:

"If the user records trying this and it fails, will I feel embarrassed to see his face?"

# Time Reality:

- Time saved skipping tests: 30 seconds
- Time wasted when it doesn't work: 30 minutes
- User trust lost: Immeasurable

A user describing a bug for the third time isn't thinking "this AI is trying hard" - they're thinking "why am I wasting time with this incompetent tool?"

---

<!-- devplan:start -->
## Phase: Core Backend Setup & Schema Discovery
- [ ] Updated multi-module Maven project structure with correct Spring Boot/Spring AI versions.
- [ ] Rename `mcp-db-client` to `plumchat-client` for clarity.
- [ ] A functional `mcp-schema-server` that can connect to a Greenplum database and expose schema information via MCP tools.
- [ ] Test the `mcp-schema-server` using the existing `mcp-client` to validate the schema retrieval functionality.
- [ ] Unit and integration tests for the `mcp-schema-server`.

## Phase: Query Execution
- [ ] A functional `mcp-query-server` that can execute SQL queries against the Greenplum database.
- [ ] Test the `mcp-query-server` using the existing `mcp-client` to validate SQL execution.
- [ ] Begin development of `plumchat-client` with Spring AI integration for natural language to SQL translation.
- [ ] A user approval workflow for SQL queries within the `plumchat-client`.
- [ ] Integration between `plumchat-client` and both MCP servers (schema + query).
- [ ] Tests for the query translation and execution process.

## Phase: Management Functionality
- [ ] A functional `mcp-mgmt-server` with tools for `gpstart` and `gpstop`.
- [ ] Test the `mcp-mgmt-server` using the existing `mcp-client` to validate administrative operations.
- [ ] Integration of management tools into `plumchat-client` with proper user authorization.
- [ ] A robust security model for the `mcp-mgmt-server` to ensure that only authorized users can perform administrative tasks.
- [ ] Tests for the management tools.

## Phase: Frontend and Authentication
- [ ] Create `plumchat-ui` Maven module with React chat interface.
- [ ] Implement Maven build integration to bundle React app into `plumchat-client` static resources.
- [ ] A secure authentication system based on Greenplum credentials in `plumchat-client`.
- [ ] REST APIs in `plumchat-client` for UI communication (chat, auth, database selection).
- [ ] End-to-end tests for the authentication and chat functionality.

## Phase: Deployment and Documentation
- [ ] Dockerfiles for each of the services.
- [ ] Docker Compose or Kubernetes scripts for deployment.
- [ ] Comprehensive documentation for developers and users.
- [ ] A finalized `README.md` with a project log.
<!-- devplan:end -->

## Phase Descriptions History

### Phase 1: Core Backend Setup & Schema Discovery
**Description**: This phase focuses on setting up the multi-module Maven project and implementing the `mcp-schema-server`. This server will be responsible for discovering and providing database schema information to the `mcp-client`. We will also create the initial `mcp-client` application that can communicate with the schema server.

**Libraries/Frameworks**: Spring Boot, Spring AI, Spring WebFlux, JDBC, Maven

### Phase 2: Query Execution
**Description**: In this phase, we will implement the `mcp-query-server` to enable natural language querying. The `mcp-client` will be enhanced to translate natural language questions into SQL, present the SQL to the user for approval, and then send it to the `mcp-query-server` for execution.

**Libraries/Frameworks**: Spring AI, JDBC

### Phase 3: Management Functionality
**Description**: This phase involves implementing the `mcp-mgmt-server` to expose administrative tasks as tools. We will focus on securely implementing tools for starting and stopping the Greenplum database.

**Libraries/Frameworks**: Spring Boot

### Phase 4: Frontend and Authentication
**Description**: This phase is about building the user interface and implementing the authentication system. We will create the `plumchat-ui` React module and integrate it with `plumchat-client`. We will also implement the Greenplum-based authentication using Spring Security and JWT.

**Libraries/Frameworks**: React, Spring Security, Maven Frontend Plugin

### Phase 5: Deployment and Documentation
**Description**: The final phase focuses on preparing the application for deployment. We will containerize the services, create deployment scripts, and finalize all project documentation.

**Libraries/Frameworks**: Docker
