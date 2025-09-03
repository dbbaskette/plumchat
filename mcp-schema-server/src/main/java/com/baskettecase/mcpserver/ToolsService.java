package com.baskettecase.mcpserver;

import com.baskettecase.mcpserver.model.SchemaInfo;
import com.baskettecase.mcpserver.model.TableInfo;
import com.baskettecase.mcpserver.service.SchemaDiscoveryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;


import java.sql.SQLException;
import java.util.List;

@Service
public class ToolsService {

    private static final Logger logger = LoggerFactory.getLogger(ToolsService.class);

    private final SchemaDiscoveryService schemaDiscoveryService;
    private final ObjectMapper objectMapper;

    public ToolsService(SchemaDiscoveryService schemaDiscoveryService) {
        this.schemaDiscoveryService = schemaDiscoveryService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get all database schemas with their table names
     * @return JSON string containing all schemas and their tables
     */
    @Tool(description = "Get all database schemas with their table names. Returns comprehensive schema information for database exploration.")
    public String getAllSchemas() {
        try {
            List<SchemaInfo> schemas = schemaDiscoveryService.getAllSchemas();
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schemas);
        } catch (SQLException e) {
            logger.error("Failed to retrieve schemas", e);
            return "Error retrieving schemas: " + e.getMessage();
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize schemas to JSON", e);
            return "Error serializing schema information: " + e.getMessage();
        }
    }

    /**
     * Get detailed information about all tables in a specific schema
     * @param schemaName The name of the schema to explore
     * @return JSON string containing detailed table information including columns, data types, and constraints
     */
    @Tool(description = "Get detailed information about all tables in a specific schema. Includes columns, data types, primary keys, foreign keys, and table metadata.")
    public String getTablesInSchema(
        @org.springframework.ai.tool.annotation.ToolParam(description = "The name of the database schema to query") String schemaName) {
        logger.debug("getTablesInSchema called with schemaName: '{}'", schemaName);
        
        if (schemaName == null || schemaName.trim().isEmpty()) {
            logger.warn("getTablesInSchema received null or empty schemaName: '{}'", schemaName);
            return "Error: Schema name cannot be null or empty";
        }

        try {
            List<TableInfo> tables = schemaDiscoveryService.getTablesInSchema(schemaName.trim());
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tables);
        } catch (SQLException e) {
            logger.error("Failed to retrieve tables for schema: {}", schemaName, e);
            return "Error retrieving tables for schema '" + schemaName + "': " + e.getMessage();
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize table information to JSON", e);
            return "Error serializing table information: " + e.getMessage();
        }
    }

    /**
     * Get detailed information about a specific table
     * @param schemaName The schema containing the table
     * @param tableName The name of the table
     * @return JSON string containing detailed table information including all columns and their properties
     */
    @Tool(description = "Get detailed information about a specific table. Returns comprehensive column information including data types, constraints, nullable status, and relationships.")
    public String getTableInfo(
        @org.springframework.ai.tool.annotation.ToolParam(description = "The name of the database schema") String schemaName,
        @org.springframework.ai.tool.annotation.ToolParam(description = "The name of the table") String tableName) {
        logger.debug("getTableInfo called with schemaName: '{}', tableName: '{}'", schemaName, tableName);
        
        if (schemaName == null || schemaName.trim().isEmpty()) {
            logger.warn("getTableInfo received null or empty schemaName: '{}'", schemaName);
            return "Error: Schema name cannot be null or empty";
        }
        if (tableName == null || tableName.trim().isEmpty()) {
            logger.warn("getTableInfo received null or empty tableName: '{}'", tableName);
            return "Error: Table name cannot be null or empty";
        }

        try {
            TableInfo table = schemaDiscoveryService.getTableInfo(schemaName.trim(), tableName.trim());
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(table);
        } catch (SQLException e) {
            logger.error("Failed to retrieve table info for {}.{}", schemaName, tableName, e);
            return "Error retrieving table information for '" + schemaName + "." + tableName + "': " + e.getMessage();
        } catch (IllegalArgumentException e) {
            logger.warn("Table not found: {}.{}", schemaName, tableName);
            return "Table not found: " + schemaName + "." + tableName;
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize table information to JSON", e);
            return "Error serializing table information: " + e.getMessage();
        }
    }
}