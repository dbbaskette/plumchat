package com.baskettecase.plumchat.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class McpToolDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(McpToolDiscoveryService.class);

    @Autowired
    private Environment environment;

    @Autowired
    private ChatClient chatClient;

    @EventListener(ApplicationReadyEvent.class)
    public void logMcpToolDiscovery() {
        logger.info("🚀 PlumChat Client started successfully!");
        logger.info("🌐 Client running on port: {}", environment.getProperty("server.port", "8090"));
        logger.info("🎯 UI available at: http://localhost:{}", environment.getProperty("server.port", "8090"));
        
        logger.info("🔍 MCP Client Configuration:");
        logger.info("   Enabled: {}", environment.getProperty("spring.ai.mcp.client.enabled", "false"));
        logger.info("   Name: {}", environment.getProperty("spring.ai.mcp.client.name", "Not configured"));
        logger.info("   Version: {}", environment.getProperty("spring.ai.mcp.client.version", "Not configured"));
        logger.info("   Type: {}", environment.getProperty("spring.ai.mcp.client.type", "Not configured"));
        logger.info("   Request Timeout: {}", environment.getProperty("spring.ai.mcp.client.request-timeout", "Not configured"));
        logger.info("   Tool Callback Enabled: {}", environment.getProperty("spring.ai.mcp.client.toolcallback.enabled", "false"));
        
        logger.info("🔗 MCP Server Connections:");
        logger.info("   Schema Server: {}", environment.getProperty("spring.ai.mcp.client.sse.connections.schema-server.url", "Not configured"));
        logger.info("   Schema Server SSE: {}", environment.getProperty("spring.ai.mcp.client.sse.connections.schema-server.sse-endpoint", "Not configured"));
        
        // Check for other MCP server connections
        String queryServerUrl = environment.getProperty("spring.ai.mcp.client.sse.connections.query-server.url");
        if (queryServerUrl != null) {
            logger.info("   Query Server: {}", queryServerUrl);
            logger.info("   Query Server SSE: {}", environment.getProperty("spring.ai.mcp.client.sse.connections.query-server.sse-endpoint", "Not configured"));
        }
        
        logger.info("🛠️  MCP Tool Auto-Discovery:");
        logger.info("   Spring AI will auto-discover tools from connected MCP servers");
        logger.info("   Available tools depend on active MCP server connections");
        logger.info("   Tools are dynamically loaded and integrated into ChatClient");
        
        logger.info("🌟 PlumChat initialization complete!");
        logger.info("💡 Expected MCP tools from connected servers:");
        logger.info("   📊 Schema Discovery Tools (from schema-server on port 8080):");
        logger.info("      - getAllSchemas: Get all database schemas");
        logger.info("      - getTablesInSchema: Get tables in a specific schema");
        logger.info("      - getTableInfo: Get detailed table information");
        logger.info("      - testMcpConnection: Test MCP server connectivity");
        logger.info("   🔍 Query Execution Tools (from query-server on port 8081 - if configured):");
        logger.info("      - executeQuery: Execute SELECT queries against the database");
        logger.info("      - explainQuery: Get execution plan for a query");
        logger.info("      - countTableRows: Count rows in a specified table");
        logger.info("      - testConnection: Test database connectivity");
        
        logger.info("🎉 PlumChat is ready to assist with Greenplum database operations!");
    }
}