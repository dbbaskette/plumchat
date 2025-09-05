package com.baskettecase.mcpserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class DatabaseConnectivityService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectivityService.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Environment environment;

    @EventListener(ApplicationReadyEvent.class)
    public void checkDatabaseConnectivity() {
        logger.info("🚀 MCP Query Server started successfully!");
        logger.info("🌐 Server running on port: {}", environment.getProperty("server.port", "8081"));
        logger.info("📡 MCP tools endpoints available at: http://localhost:{}", environment.getProperty("server.port", "8081"));
        
        logger.info("🔍 Checking database connectivity...");
        
        try (Connection connection = dataSource.getConnection()) {
            logger.info("✅ Database connection successful!");
            
            // Get database information
            DatabaseMetaData metaData = connection.getMetaData();
            logger.info("📊 Database: {} {}", 
                metaData.getDatabaseProductName(), 
                metaData.getDatabaseProductVersion());
            logger.info("🔗 JDBC URL: {}", metaData.getURL());
            logger.info("👤 User: {}", metaData.getUserName());
            
            // Test basic schema access
            logger.info("🔍 Testing schema access...");
            try (ResultSet schemas = metaData.getSchemas()) {
                int schemaCount = 0;
                while (schemas.next() && schemaCount < 5) { // Limit to first 5 schemas
                    String schemaName = schemas.getString("TABLE_SCHEM");
                    logger.info("   📂 Found schema: {}", schemaName);
                    schemaCount++;
                }
                
                if (schemaCount > 0) {
                    logger.info("✅ Query execution ready - found {} schemas", schemaCount);
                    logger.info("🛠️  MCP query tools are ready for database operations!");
                } else {
                    logger.warn("⚠️  No schemas found - database may be empty or access restricted");
                }
            }
            
        } catch (SQLException e) {
            logger.error("❌ Database connection failed: {}", e.getMessage());
            logger.error("🔧 Database configuration:");
            logger.error("   URL: {}", environment.getProperty("spring.datasource.url", "Not configured"));
            logger.error("   Username: {}", environment.getProperty("spring.datasource.username", "Not configured"));
            logger.error("   Driver: {}", environment.getProperty("spring.datasource.driver-class-name", "Not configured"));
            
            logger.warn("⚠️  MCP query tools may not function properly without database connectivity");
            logger.info("💡 To fix this:");
            logger.info("   1. Ensure Greenplum database is running");
            logger.info("   2. Check database credentials in environment variables");
            logger.info("   3. Verify network connectivity to database host");
            
        } catch (Exception e) {
            logger.error("❌ Unexpected error during database check: {}", e.getMessage(), e);
        }
        
        logger.info("🌟 MCP Query Server initialization complete!");
        logger.info("📋 Available MCP tools:");
        logger.info("   - executeQuery: Execute SELECT queries against the database");
        logger.info("   - explainQuery: Get execution plan for a query");
        logger.info("   - countTableRows: Count rows in a specified table");
        logger.info("   - testConnection: Test database connectivity");
    }
}