<img src="./assets/logo.png" alt="Logo" width="400">

# PlumChat

> **Project Log:** A project log will be maintained here to track notable changes and versions.

## Overview

PlumChat is an agentic application that leverages a chat interface and several MCP tools to provide a natural language interface to a Greenplum (PostgreSQL) database. The application features a central AI agent (the MCP Client) that leverages specialized tools and data sources (MCP Servers) and a chat interface to allow users to interact with their Greenplum databases.

## Features

- Natural language querying of Greenplum databases
- Database schema exploration
- Administrative tasks (start/stop database)
- Secure authentication against Greenplum
- Chat-based user interface

## Getting Started

[Instructions to be added]

## Project Structure

- `mcp-client/`: Generic MCP testing client
- `plumchat-client/`: Main PlumChat Host application
- `mcp-schema-server/`: MCP Server for schema information
- `mcp-query-server/`: MCP Server for query execution
- `mcp-mgmt-server/`: MCP Server for management tasks

## Tech Stack

- Java 21
- Spring Boot 3.5.5
- Spring AI 1.0.1
- React (frontend)
- Maven

## Contributing

[Contributing guidelines to be added]

## License

[License information to be added]
