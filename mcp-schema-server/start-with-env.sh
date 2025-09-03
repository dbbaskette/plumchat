#!/bin/bash

# MCP Schema Server Startup Script with Environment Variables
# Usage: ./start-with-env.sh [--sse|--stdio]

set -e

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 Starting MCP Schema Server with Environment Configuration${NC}"

# Function to kill existing MCP server processes
kill_existing_servers() {
    echo -e "${BLUE}🔍 Checking for existing MCP server processes...${NC}"
    
    # Find Java processes running our specific JAR file
    local jar_name="mcp-schema-server-0.0.1.jar"
    local existing_pids=$(ps aux | grep "$jar_name" | grep -v grep | awk '{print $2}')
    
    if [ -n "$existing_pids" ]; then
        echo -e "${YELLOW}⚠️ Found existing MCP schema server processes:${NC}"
        echo "$existing_pids" | while read -r pid; do
            if [ -n "$pid" ]; then
                echo -e "  ${YELLOW}🔥 Killing PID: $pid${NC}"
                kill "$pid" 2>/dev/null || true
            fi
        done
        
        # Wait a moment for processes to terminate gracefully
        sleep 3
        
        # Force kill any remaining processes
        local remaining_pids=$(ps aux | grep "$jar_name" | grep -v grep | awk '{print $2}')
        if [ -n "$remaining_pids" ]; then
            echo -e "${YELLOW}🔨 Force killing remaining processes...${NC}"
            echo "$remaining_pids" | while read -r pid; do
                if [ -n "$pid" ]; then
                    kill -9 "$pid" 2>/dev/null || true
                fi
            done
            sleep 1
        fi
        
        echo -e "${GREEN}✓ Existing MCP schema server processes terminated${NC}"
    else
        echo -e "${GREEN}✓ No existing MCP schema server processes found${NC}"
    fi
    
    # Also check for processes using port 8080 (SSE mode)
    local port_pid=$(lsof -ti tcp:8080 2>/dev/null)
    if [ -n "$port_pid" ]; then
        echo -e "${YELLOW}🔥 Found process using port 8080 (PID: $port_pid), killing...${NC}"
        kill "$port_pid" 2>/dev/null || true
        sleep 2
        # Force kill if still running
        if kill -0 "$port_pid" 2>/dev/null; then
            kill -9 "$port_pid" 2>/dev/null || true
        fi
        echo -e "${GREEN}✓ Process using port 8080 terminated${NC}"
    fi
    
    echo -e ""
}

# Kill any existing instances first
kill_existing_servers

# Load environment variables from project root .env file
ENV_FILE="$PROJECT_ROOT/.env"
if [ -f "$ENV_FILE" ]; then
    echo -e "${GREEN}✓${NC} Loading environment variables from: $ENV_FILE"
    
    # Export variables from .env file, handling comments and empty lines
    while IFS= read -r line || [ -n "$line" ]; do
        # Skip comments and empty lines
        [[ "$line" =~ ^[[:space:]]*# ]] && continue
        [[ -z "$line" ]] && continue
        [[ "$line" =~ ^[[:space:]]*$ ]] && continue
        
        # Export the variable
        if [[ "$line" =~ ^[[:space:]]*([^=]+)=(.*)$ ]]; then
            var_name="${BASH_REMATCH[1]}"
            var_value="${BASH_REMATCH[2]}"
            export "$var_name"="$var_value"
            echo -e "  ${GREEN}•${NC} $var_name"
        fi
    done < "$ENV_FILE"
else
    echo -e "${YELLOW}⚠️${NC} No .env file found at: $ENV_FILE"
    echo -e "${YELLOW}⚠️${NC} Using default database configuration"
fi

# Default transport mode
TRANSPORT_MODE="sse"
JAR_FILE="$SCRIPT_DIR/target/mcp-schema-server-0.0.1.jar"

# Parse command line arguments
for arg in "$@"; do
    case $arg in
        --sse)
            TRANSPORT_MODE="sse"
            shift
            ;;
        --stdio)
            TRANSPORT_MODE="stdio"
            shift
            ;;
        -h|--help)
            echo "Usage: $0 [--sse|--stdio]"
            echo ""
            echo "Options:"
            echo "  --sse     Use Server-Sent Events transport (default, for web clients)"
            echo "  --stdio   Use Standard I/O transport (for Claude Desktop)"
            echo ""
            echo "Environment variables are loaded from: $ENV_FILE"
            echo ""
            echo "Database configuration:"
            echo "  GREENPLUM_URL  - Database connection URL"
            echo "  GREENPLUM_USER - Database username" 
            echo "  GREENPLUM_PASSWORD - Database password"
            exit 0
            ;;
        *)
            echo -e "${RED}❌ Unknown option: $arg${NC}"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Check if JAR exists
if [ ! -f "$JAR_FILE" ]; then
    echo -e "${RED}❌ JAR file not found: $JAR_FILE${NC}"
    echo -e "${YELLOW}💡 Run: mvn clean package -DskipTests${NC}"
    exit 1
fi

# Display configuration
echo -e "${BLUE}⚙️ Configuration:${NC}"
echo -e "  Transport Mode: ${YELLOW}$TRANSPORT_MODE${NC}"
echo -e "  JAR File: ${YELLOW}$JAR_FILE${NC}"
echo -e "  Database URL: ${YELLOW}${GREENPLUM_URL:-'Not set (will use default)'}${NC}"
echo -e "  Database User: ${YELLOW}${GREENPLUM_USER:-'Not set (will use default)'}${NC}"

# Set Spring profile based on transport mode
SPRING_PROFILE="$TRANSPORT_MODE"
JVM_ARGS="-Dspring.profiles.active=$SPRING_PROFILE"

echo -e ""
echo -e "${GREEN}🚀 Starting MCP Schema Server...${NC}"
echo -e "  Mode: ${YELLOW}$TRANSPORT_MODE${NC}"
echo -e "  Profile: ${YELLOW}$SPRING_PROFILE${NC}"
echo -e ""

if [ "$TRANSPORT_MODE" = "sse" ]; then
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${GREEN}🌐 Server will be available at: http://localhost:8080${NC}"
    echo -e "${GREEN}🔗 SSE Endpoint: http://localhost:8080/sse${NC}"
    echo -e "${GREEN}📡 MCP Endpoint: http://localhost:8080/mcp/message${NC}"
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}💡 Press Ctrl+C to stop the server${NC}"
    echo -e ""
else
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${GREEN}📟 STDIO Mode - Ready for Claude Desktop connection${NC}"
    echo -e "${GREEN}📋 Add this configuration to Claude Desktop:${NC}"
    echo -e "${YELLOW}    \"mcp-schema-server\": {${NC}"
    echo -e "${YELLOW}      \"command\": \"java\",${NC}"
    echo -e "${YELLOW}      \"args\": [\"-Dspring.profiles.active=stdio\", \"-jar\", \"$JAR_FILE\"]${NC}"
    echo -e "${YELLOW}    }${NC}"
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}💡 Press Ctrl+C to stop the server${NC}"
    echo -e ""
fi

# Start the server
exec java $JVM_ARGS -jar "$JAR_FILE"