#!/bin/bash

# Simple script to run the MCP Query Server
# Usage: ./run-query-server.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR"

# Load environment variables from .env file
ENV_FILE="$PROJECT_ROOT/.env"
if [ -f "$ENV_FILE" ]; then
    echo "Loading environment variables from: $ENV_FILE"
    
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
            echo "  • $var_name"
        fi
    done < "$ENV_FILE"
else
    echo "⚠️ No .env file found at: $ENV_FILE"
    echo "⚠️ Using default database configuration"
fi

echo ""
echo "🚀 Starting MCP Query Server..."
echo "  Mode: SSE"
echo "  Database URL: ${GREENPLUM_URL:-'jdbc:postgresql://localhost:5432/gpadmin'}"
echo "  Database User: ${GREENPLUM_USER:-'gpadmin'}"
echo ""
echo "🌐 Server will be available at: http://localhost:8080"
echo "📡 MCP Endpoint: http://localhost:8080/mcp/message"
echo ""
echo "💡 Press Ctrl+C to stop the server"
echo ""

exec "$SCRIPT_DIR/mcp-query-server/start-with-env.sh" --sse