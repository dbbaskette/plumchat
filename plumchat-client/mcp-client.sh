#!/bin/bash

# MCP Client - A Spring AI MCP Client Tool for Testing MCP Servers
# Usage: ./mcp-client [options]

set -e

# Default values
PROFILE="stdio"
VERBOSE=false
REBUILD=false

# Function to get jar name dynamically
get_jar_name() {
    if [[ -d "target" ]]; then
        # Find the jar file in target directory (excluding sources and javadoc jars)
        local jar_file=$(find target -name "*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" | head -1)
        if [[ -n "$jar_file" ]]; then
            basename "$jar_file"
        else
            # If no JAR found, it might be because of a clean. We'll build later.
            return
        fi
    fi
}

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print usage
usage() {
    echo "MCP Client - An interactive tool for testing MCP servers."
    echo ""
    echo "USAGE:"
    echo "    ./mcp-client.sh [OPTIONS]"
    echo ""
    echo "OPTIONS:"
    echo "    -p, --profile PROFILE       Transport profile: stdio, sse, streamable (default: stdio)"
    echo "    --rebuild                   Force a clean rebuild of the application"
    echo "    -h, --help                  Show this help message"
    echo "    -v, --verbose               Enable verbose output (sets logging to DEBUG)"
}

# Function to log messages
log() {
    if [[ "$VERBOSE" == "true" ]]; then
        echo -e "${BLUE}[INFO]${NC} $1"
    fi
}

error() {
    echo -e "${RED}[ERROR]${NC} $1" >&2
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -p|--profile)
            PROFILE="$2"
            shift 2
            ;;
        --rebuild)
            REBUILD=true
            shift
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        *)
            error "Unknown option: $1"
            usage
            exit 1
            ;;
    esac
done

# Validate profile
case $PROFILE in
    stdio|sse|streamable)
        ;;
    *)
        error "Invalid profile: $PROFILE. Must be one of: stdio, sse, streamable"
        exit 1
        ;;
esac

# Build the application if needed or forced
build_if_needed() {
    local jar_name=$(get_jar_name)
    
    if [[ "$REBUILD" == "true" ]] || [[ -z "$jar_name" ]]; then
        if [[ "$REBUILD" == "true" ]]; then
            log "Rebuild requested. Cleaning and packaging..."
        else
            warn "JAR not found. Building application..."
        fi
        
        if ! ./mvnw clean package; then
            error "Build failed."
            exit 1
        fi
        success "Build completed successfully"
    else
        log "Using existing JAR: target/$jar_name"
    fi
}

echo "🤖 MCP Client - Direct MCP Server Testing"
echo

build_if_needed

jar_name=$(get_jar_name)
if [[ -z "$jar_name" ]]; then
    error "Could not find or build the application JAR. Exiting."
    exit 1
fi

jar_path="target/$jar_name"
java_props="-Dspring.profiles.active=$PROFILE"

if [[ "$VERBOSE" == "true" ]]; then
    java_props="$java_props -Dlogging.level.org.springframework.ai.mcp=DEBUG"
fi

echo "Starting Interactive MCP Client..."
echo "Profile: $PROFILE"
echo

# Run the application
log "Executing: java $java_props -jar $jar_path"

java $java_props -jar "$jar_path"
