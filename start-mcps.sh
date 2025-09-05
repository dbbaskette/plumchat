#!/bin/bash

# Combined startup script for MCP servers
# Usage: ./start-mcps.sh [command]

set -e

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Server configurations
SCHEMA_SERVER_DIR="$SCRIPT_DIR/mcp-schema-server"
QUERY_SERVER_DIR="$SCRIPT_DIR/mcp-query-server"

SCHEMA_SERVER_LOG="$SCRIPT_DIR/mcp-schema-server.log"
QUERY_SERVER_LOG="$SCRIPT_DIR/mcp-query-server.log"

SCHEMA_SERVER_PID_FILE="$SCRIPT_DIR/mcp-schema-server.pid"
QUERY_SERVER_PID_FILE="$SCRIPT_DIR/mcp-query-server.pid"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print usage
print_usage() {
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  start [--build]   Start the MCP servers. Use --build to build first."
    echo "  stop              Stop the MCP servers."
    echo "  status            Check the status of the MCP servers."
    echo "  logs              Tail the logs of the MCP servers."
    echo "  help              Show this help message."
}

# Function to build the project
build_project() {
    echo -e "${BLUE}🔨 Building project with Maven...${NC}"
    echo -e "  Running: ${YELLOW}mvn clean package -DskipTests${NC}"
    echo -e ""
    
    if mvn clean package -DskipTests; then
        echo -e "${GREEN}✓ Build completed successfully${NC}"
        echo -e ""
    else
        echo -e "${RED}❌ Build failed${NC}"
        exit 1
    fi
}

# Function to find the latest JAR file for a server
find_jar() {
    local server_dir="$1"
    local jar_pattern="$2"
    local jar_file=$(find "$server_dir/target" -name "$jar_pattern" -type f -printf '%T@ %p\n' 2>/dev/null | sort -n | tail -1 | cut -d' ' -f2-)
    
    if [ -z "$jar_file" ]; then
        jar_file=$(ls -t "$server_dir/target"/$jar_pattern 2>/dev/null | head -1)
    fi
    
    if [ -z "$jar_file" ] || [ ! -f "$jar_file" ]; then
        echo -e "${RED}❌ No JAR file found in $server_dir/target${NC}"
        echo -e "${YELLOW}💡 Try running with the --build option${NC}"
        exit 1
    fi
    
    echo "$jar_file"
}

# Function to kill existing server processes
kill_server() {
    local pid_file="$1"
    local server_name="$2"

    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if kill -0 "$pid" 2>/dev/null; then
            echo -e "${YELLOW}🔥 Stopping $server_name (PID: $pid)...${NC}"
            kill "$pid"
            rm "$pid_file"
        else
            echo -e "${GREEN}✓ $server_name is not running.${NC}"
            rm "$pid_file"
        fi
    else
        echo -e "${GREEN}✓ $server_name is not running.${NC}"
    fi
}

# Function to start the servers
start_servers() {
    echo -e "${BLUE}🚀 Starting MCP servers...${NC}"

    # Load environment variables
    ENV_FILE="$SCRIPT_DIR/.env"
    if [ -f "$ENV_FILE" ]; then
        echo -e "${GREEN}✓${NC} Loading environment variables from: $ENV_FILE"
        export $(grep -v '^#' "$ENV_FILE" | xargs)
    else
        echo -e "${YELLOW}⚠️ No .env file found. Servers will use default settings.${NC}"
    fi

    # Stop existing servers
    kill_server "$SCHEMA_SERVER_PID_FILE" "MCP Schema Server"
    kill_server "$QUERY_SERVER_PID_FILE" "MCP Query Server"

    # Find JARs
    local schema_jar=$(find_jar "$SCHEMA_SERVER_DIR" "mcp-schema-server-*.jar")
    local query_jar=$(find_jar "$QUERY_SERVER_DIR" "mcp-query-server-*.jar")

    # Start Schema Server
    echo -e "${BLUE}Starting MCP Schema Server...${NC}"
    nohup java -Dspring.profiles.active=sse -jar "$schema_jar" > "$SCHEMA_SERVER_LOG" 2>&1 &
    echo $! > "$SCHEMA_SERVER_PID_FILE"
    echo -e "${GREEN}✓ MCP Schema Server started (PID: $(cat "$SCHEMA_SERVER_PID_FILE")). Log: $SCHEMA_SERVER_LOG${NC}"

    # Start Query Server
    echo -e "${BLUE}Starting MCP Query Server...${NC}"
    nohup java -jar "$query_jar" > "$QUERY_SERVER_LOG" 2>&1 &
    echo $! > "$QUERY_SERVER_PID_FILE"
    echo -e "${GREEN}✓ MCP Query Server started (PID: $(cat "$QUERY_SERVER_PID_FILE")). Log: $QUERY_SERVER_LOG${NC}"
}

# Function to stop the servers
stop_servers() {
    echo -e "${BLUE}🛑 Stopping MCP servers...${NC}"
    kill_server "$SCHEMA_SERVER_PID_FILE" "MCP Schema Server"
    kill_server "$QUERY_SERVER_PID_FILE" "MCP Query Server"
}

# Function to check server status
check_status() {
    echo -e "${BLUE}📊 MCP Server Status:${NC}"
    # Schema Server
    if [ -f "$SCHEMA_SERVER_PID_FILE" ]; then
        local pid=$(cat "$SCHEMA_SERVER_PID_FILE")
        if kill -0 "$pid" 2>/dev/null; then
            echo -e "  ${GREEN}● MCP Schema Server is running (PID: $pid)${NC}"
        else
            echo -e "  ${RED}● MCP Schema Server is NOT running.${NC}"
        fi
    else
        echo -e "  ${RED}● MCP Schema Server is NOT running.${NC}"
    fi

    # Query Server
    if [ -f "$QUERY_SERVER_PID_FILE" ]; then
        local pid=$(cat "$QUERY_SERVER_PID_FILE")
        if kill -0 "$pid" 2>/dev/null; then
            echo -e "  ${GREEN}● MCP Query Server is running (PID: $pid)${NC}"
        else
            echo -e "  ${RED}● MCP Query Server is NOT running.${NC}"
        fi
    else
        echo -e "  ${RED}● MCP Query Server is NOT running.${NC}"
    fi
}

# Function to tail logs
tail_logs() {
    echo -e "${BLUE}📜 Tailing logs for both servers... (Press Ctrl+C to stop)${NC}"
    tail -f "$SCHEMA_SERVER_LOG" "$QUERY_SERVER_LOG"
}

# Main script logic
case "$1" in
    start)
        if [ "$2" == "--build" ]; then
            build_project
        fi
        start_servers
        ;;
    stop)
        stop_servers
        ;;
    status)
        check_status
        ;;
    logs)
        tail_logs
        ;;
    help|--help)
        print_usage
        ;;
    *)
        print_usage
        exit 1
        ;;
esac

exit 0
