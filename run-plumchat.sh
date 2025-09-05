#!/bin/bash

# PlumChat Client Management Script
# This script kills old processes, rebuilds the application, and starts it

set -e  # Exit on any error

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "🌱 PlumChat Client Management Script"
echo "==================================="

# Function to kill existing PlumChat processes
kill_plumchat() {
    echo "🔍 Looking for existing PlumChat processes..."
    
    # Find Java processes running plumchat-client
    PIDS=$(pgrep -f "plumchat-client.*\.jar" || true)
    
    if [ -n "$PIDS" ]; then
        echo "🛑 Killing existing PlumChat processes: $PIDS"
        echo "$PIDS" | xargs kill -TERM
        
        # Wait a moment for graceful shutdown
        sleep 2
        
        # Force kill if still running
        REMAINING=$(pgrep -f "plumchat-client.*\.jar" || true)
        if [ -n "$REMAINING" ]; then
            echo "💀 Force killing remaining processes: $REMAINING"
            echo "$REMAINING" | xargs kill -KILL
        fi
        
        echo "✅ All PlumChat processes terminated"
    else
        echo "ℹ️  No existing PlumChat processes found"
    fi
}

# Function to build the application
build_plumchat() {
    echo ""
    echo "🔨 Building PlumChat Client..."
    echo "=============================="
    
    cd plumchat-client
    
    echo "📦 Running Maven clean package..."
    mvn clean package -DskipTests
    
    if [ $? -eq 0 ]; then
        echo "✅ Build completed successfully!"
    else
        echo "❌ Build failed!"
        exit 1
    fi
    
    cd ..
}

# Function to load environment variables from .env file
load_env() {
    if [ -f ".env" ]; then
        echo "📄 Loading environment variables from .env file..."
        
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
                echo "  ✓ $var_name"
            fi
        done < ".env"
        
        echo "✅ Environment variables loaded"
    elif [ -f "env.example" ]; then
        echo "⚠️  No .env file found. Copy env.example to .env and configure your settings:"
        echo "   cp env.example .env"
        echo "   # Edit .env with your OpenAI API key and other settings"
        echo ""
    fi
}

# Function to start the application
start_plumchat() {
    echo ""
    echo "🚀 Starting PlumChat Client..."
    echo "============================="
    
    # Load environment variables
    load_env
    
    # Check if JAR exists
    JAR_FILE="plumchat-client/target/plumchat-client-0.0.4.jar"
    if [ ! -f "$JAR_FILE" ]; then
        echo "❌ JAR file not found: $JAR_FILE"
        echo "Please run the build first."
        exit 1
    fi
    
    echo "📝 JAR file found: $(ls -lh $JAR_FILE | awk '{print $5, $9}')"
    
    # Check OpenAI configuration
    if [ -z "$OPENAI_API_KEY" ] || [ "$OPENAI_API_KEY" = "your-openai-api-key-here" ]; then
        echo ""
        echo "⚠️  OpenAI API key not configured!"
        echo "   PlumChat will use mock responses until you configure it."
        echo "   To enable real AI responses:"
        echo "   1. Copy env.example to .env: cp env.example .env"
        echo "   2. Edit .env and add your OpenAI API key"
        echo "   3. Restart PlumChat"
    else
        echo "✅ OpenAI API key configured"
    fi
    
    echo ""
    echo "🌐 Starting PlumChat on http://localhost:8090"
    echo "💡 Press Ctrl+C to stop"
    echo ""
    
    # Start the application
    cd plumchat-client
    java -jar target/plumchat-client-0.0.4.jar
}

# Main execution
main() {
    echo "Starting at: $(date)"
    echo ""
    
    # Parse command line arguments
    FORCE_BUILD=false
    SKIP_KILL=false
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --build|-b)
                FORCE_BUILD=true
                shift
                ;;
            --skip-kill|-s)
                SKIP_KILL=true
                shift
                ;;
            --help|-h)
                echo "Usage: $0 [OPTIONS]"
                echo ""
                echo "Options:"
                echo "  --build, -b     Force rebuild even if JAR exists"
                echo "  --skip-kill, -s Skip killing existing processes"
                echo "  --help, -h      Show this help message"
                echo ""
                echo "Examples:"
                echo "  $0              # Kill processes, build if needed, and run"
                echo "  $0 --build      # Kill processes, force rebuild, and run"
                echo "  $0 --skip-kill  # Just build and run (don't kill existing)"
                exit 0
                ;;
            *)
                echo "Unknown option: $1"
                echo "Use --help for usage information"
                exit 1
                ;;
        esac
    done
    
    # Step 1: Kill existing processes (unless skipped)
    if [ "$SKIP_KILL" = false ]; then
        kill_plumchat
    fi
    
    # Step 2: Build if needed or forced
    JAR_FILE="plumchat-client/target/plumchat-client-0.0.4.jar"
    if [ "$FORCE_BUILD" = true ] || [ ! -f "$JAR_FILE" ]; then
        build_plumchat
    else
        echo "ℹ️  JAR file exists, skipping build (use --build to force rebuild)"
    fi
    
    # Step 3: Start the application
    start_plumchat
}

# Check if plumchat-client directory exists
if [ ! -d "plumchat-client" ]; then
    echo "❌ plumchat-client directory not found"
    echo "Please make sure you're running this script from the PlumChat project root"
    exit 1
fi

# Run main function with all arguments
main "$@"
