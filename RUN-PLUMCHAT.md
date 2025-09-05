# Running PlumChat 🌱

This guide explains how to run the PlumChat application using the provided management script.

## Quick Start

```bash
# From the PlumChat project root
./run-plumchat.sh --build
```

This will:
1. Kill any existing PlumChat processes
2. Rebuild the application (frontend + backend)
3. Start PlumChat on http://localhost:8090

## Script Usage

### Basic Commands

```bash
# Kill old processes, build if needed, and run
./run-plumchat.sh

# Force rebuild and run
./run-plumchat.sh --build

# Just start (don't kill existing processes)
./run-plumchat.sh --skip-kill

# Show help
./run-plumchat.sh --help
```

### Options

| Option | Short | Description |
|--------|-------|-------------|
| `--build` | `-b` | Force rebuild even if JAR exists |
| `--skip-kill` | `-s` | Skip killing existing processes |
| `--help` | `-h` | Show help message |

## What the Script Does

### 1. Process Management
- Finds existing PlumChat Java processes
- Gracefully terminates them (SIGTERM)
- Force kills if necessary (SIGKILL)

### 2. Build Process
- Runs `mvn clean package -DskipTests`
- Builds React frontend with Vite
- Packages everything into a Spring Boot JAR
- Includes frontend assets in static resources

### 3. Application Startup
- Starts PlumChat on port 8090
- Serves React frontend at root path
- Provides REST APIs under `/api/`

## Accessing PlumChat

Once started, access PlumChat at:

**🌐 http://localhost:8090**

### Available Endpoints

- **Frontend**: http://localhost:8090 (React chat interface)
- **API**: http://localhost:8090/api/chat/message
- **Health**: http://localhost:8090/api/status/health
- **Connections**: http://localhost:8090/api/status/connections

## Development Workflow

### During Development
```bash
# Quick restart after code changes
./run-plumchat.sh --build

# Or run manually for debugging
cd plumchat-client
./mvnw spring-boot:run
```

### Frontend Development
For frontend-only changes, you can also run the React dev server:

```bash
cd plumchat-client/src/main/frontend
npm run dev
```

This starts a dev server on http://localhost:3000 with hot reload.

## Troubleshooting

### Common Issues

1. **Port 8090 already in use**
   ```bash
   # Kill processes on port 8090
   lsof -ti:8090 | xargs kill -9
   
   # Or use the script
   ./run-plumchat.sh --build
   ```

2. **Build failures**
   ```bash
   # Check Java version (need Java 21+)
   java -version
   
   # Check Node.js version (need Node 20+)
   node -version
   ```

3. **Frontend not loading**
   - Clear browser cache
   - Check browser console for errors
   - Verify frontend assets were built: `ls plumchat-client/target/classes/static/`

4. **MCP Connection Issues**
   - Currently mock implementations (shows as disconnected)
   - Start MCP servers first when they're available
   - Check `application.properties` for correct URLs

### Debug Mode
```bash
# Run with debug output
cd plumchat-client
./mvnw spring-boot:run -Dspring.profiles.active=debug
```

### Logs
Application logs will show:
- Spring Boot startup information
- Chat API requests/responses
- Frontend asset serving
- Mock MCP connection status

## Architecture Overview

```
PlumChat Client (Port 8090)
├── React Frontend (/)
│   ├── Modern chat interface
│   ├── Table/grid components
│   ├── Real-time status panel
│   └── Responsive design
├── Spring Boot Backend (/api/)
│   ├── Chat REST APIs
│   ├── Status endpoints
│   ├── Security configuration
│   └── Static resource serving
└── Mock MCP Integration
    ├── Schema server (planned)
    ├── Query server (planned)
    └── Management server (planned)
```

## Next Steps

1. **Setup OpenAI API** (optional for now - using mock responses)
   ```bash
   export OPENAI_API_KEY="your-api-key"
   ```

2. **Start MCP Servers** (when available)
   ```bash
   # Start schema server on 8080
   cd mcp-schema-server && ./mcp-server.sh
   
   # Start query server on 8081  
   cd mcp-query-server && ./mcp-server.sh
   
   # Start mgmt server on 8082
   cd mcp-mgmt-server && ./mcp-server.sh
   ```

3. **Enable Real MCP Integration**
   - Add Spring AI MCP dependencies when available
   - Uncomment MCP client code
   - Configure actual database connections

## Features Available Now

✅ **Working Features:**
- Modern React chat interface
- Mock AI responses based on keywords
- Advanced table/grid rendering components
- Real-time connection status monitoring
- Responsive design (mobile/desktop)
- REST API endpoints
- Static frontend serving

🚧 **Coming Soon:**
- Real OpenAI integration
- Live MCP server connections
- Database schema discovery
- SQL query execution
- User authentication
- Session persistence

Enjoy using PlumChat! 🌱
