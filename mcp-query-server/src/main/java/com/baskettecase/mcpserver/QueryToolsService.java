package com.baskettecase.mcpserver;

import com.baskettecase.mcpserver.model.QueryResult;
import com.baskettecase.mcpserver.service.QueryExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class QueryToolsService {

    private static final Logger logger = LoggerFactory.getLogger(QueryToolsService.class);

    private final QueryExecutionService queryExecutionService;

    public QueryToolsService(QueryExecutionService queryExecutionService) {
        this.queryExecutionService = queryExecutionService;
    }

    /**
     * Execute a SQL SELECT query against the database
     * @param sql The SQL query to execute (must be a SELECT statement)
     * @param maxRows Maximum number of rows to return (optional, defaults to 1000, max 10000)
     * @return JSON string containing query results including columns and data
     */
    @Tool(description = "Execute a SQL SELECT query against the database. Only SELECT queries are allowed for security. Returns a formatted table showing the query results with column headers and data rows, plus execution statistics.")
    public String executeQuery(
        @org.springframework.ai.tool.annotation.ToolParam(description = "The SQL SELECT query to execute") String sql,
        @org.springframework.ai.tool.annotation.ToolParam(description = "Maximum number of rows to return (optional, default 1000, max 10000)") Integer maxRows) {
        logger.info("🔧 MCP Tool called: executeQuery(sql='{}...', maxRows={})", 
                   sql != null ? sql.substring(0, Math.min(50, sql.length())) : "null", maxRows);
        
        if (sql == null || sql.trim().isEmpty()) {
            logger.warn("⚠️  executeQuery received null or empty SQL: '{}'", sql);
            return "Error: SQL query cannot be null or empty";
        }

        try {
            QueryResult result = queryExecutionService.executeQuery(sql.trim(), maxRows);
            logger.info("✅ Successfully executed query, returned {} rows in {} ms", 
                       result.getRowCount(), result.getExecutionTimeMs());
            
            // Return structured JSON that includes both readable text and structured data
            Map<String, Object> structuredResponse = new HashMap<>();
            structuredResponse.put("type", "query_result");
            structuredResponse.put("executionTimeMs", result.getExecutionTimeMs());
            structuredResponse.put("rowCount", result.getRowCount());
            
            // Human-readable message
            StringBuilder message = new StringBuilder();
            message.append("Query executed successfully!\n\n");
            message.append("Execution time: ").append(result.getExecutionTimeMs()).append(" ms\n");
            message.append("Rows returned: ").append(result.getRowCount()).append("\n");
            
            if (result.getRowCount() > 0) {
                message.append("\nFound ").append(result.getRowCount()).append(" results.");
            } else {
                message.append("\nNo rows returned.");
            }
            
            structuredResponse.put("message", message.toString());
            
            // Structured data for UI table rendering
            if (result.getColumnNames() != null) {
                structuredResponse.put("columnNames", result.getColumnNames());
                
                // Add column metadata if available
                if (result.getColumnMetadata() != null) {
                    structuredResponse.put("columnMetadata", result.getColumnMetadata());
                }
                
                if (result.getRowCount() > 0 && result.getRows() != null) {
                    // Convert rows from List<Map<String, Object>> to List<List<Object>>
                    List<List<Object>> rowData = new ArrayList<>();
                    for (Map<String, Object> row : result.getRows()) {
                        List<Object> rowValues = new ArrayList<>();
                        for (String column : result.getColumnNames()) {
                            rowValues.add(row.get(column));
                        }
                        rowData.add(rowValues);
                    }
                    structuredResponse.put("rows", rowData);
                }
            }
            
            // Convert to JSON and return
            try {
                ObjectMapper mapper = new ObjectMapper();
                String jsonResult = mapper.writeValueAsString(structuredResponse);
                logger.debug("📤 Returning structured query result: {} characters", jsonResult.length());
                return jsonResult;
            } catch (Exception e) {
                logger.error("Failed to serialize structured response", e);
                // Fallback to simple message
                return message.toString();
            }
        } catch (SQLException e) {
            logger.error("❌ Failed to execute query", e);
            return "SQL Error: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️  Invalid query: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Get the execution plan for a SQL SELECT query
     * @param sql The SQL query to explain (must be a SELECT statement)
     * @return JSON string containing the query execution plan
     */
    @Tool(description = "Get the execution plan for a SQL SELECT query using EXPLAIN. Shows how the database will execute the query, useful for performance analysis.")
    public String explainQuery(
        @org.springframework.ai.tool.annotation.ToolParam(description = "The SQL SELECT query to explain") String sql) {
        logger.info("🔧 MCP Tool called: explainQuery(sql='{}...')", 
                   sql != null ? sql.substring(0, Math.min(50, sql.length())) : "null");
        
        if (sql == null || sql.trim().isEmpty()) {
            logger.warn("⚠️  explainQuery received null or empty SQL: '{}'", sql);
            return "Error: SQL query cannot be null or empty";
        }

        try {
            QueryResult result = queryExecutionService.explainQuery(sql.trim());
            logger.info("✅ Successfully generated query plan in {} ms", result.getExecutionTimeMs());
            
            // Format the result in a human-readable way
            StringBuilder response = new StringBuilder();
            response.append("Query plan generated successfully!\n\n");
            response.append("Execution time: ").append(result.getExecutionTimeMs()).append(" ms\n\n");
            
            if (result.getRowCount() > 0 && result.getRows() != null) {
                response.append("Query Execution Plan:\n");
                for (Map<String, Object> row : result.getRows()) {
                    for (Map.Entry<String, Object> entry : row.entrySet()) {
                        response.append(entry.getValue() != null ? entry.getValue().toString() : "NULL");
                        response.append("\n");
                    }
                }
            }
            
            return response.toString();
        } catch (SQLException e) {
            logger.error("❌ Failed to explain query", e);
            return "SQL Error: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️  Invalid query for explain: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Execute a simple query to count rows in a table
     * @param schemaName The schema containing the table
     * @param tableName The name of the table to count
     * @return JSON string containing the row count result
     */
    @Tool(description = "Count the number of rows in a specific table. Executes SELECT COUNT(*) FROM schema.table.")
    public String countTableRows(
        @org.springframework.ai.tool.annotation.ToolParam(description = "The name of the database schema") String schemaName,
        @org.springframework.ai.tool.annotation.ToolParam(description = "The name of the table") String tableName) {
        logger.info("🔧 MCP Tool called: countTableRows(schemaName='{}', tableName='{}')", schemaName, tableName);
        
        if (schemaName == null || schemaName.trim().isEmpty()) {
            logger.warn("⚠️  countTableRows received null or empty schemaName: '{}'", schemaName);
            return "Error: Schema name cannot be null or empty";
        }
        if (tableName == null || tableName.trim().isEmpty()) {
            logger.warn("⚠️  countTableRows received null or empty tableName: '{}'", tableName);
            return "Error: Table name cannot be null or empty";
        }

        String sql = String.format("SELECT COUNT(*) as row_count FROM %s.%s", 
                                 schemaName.trim(), tableName.trim());
        
        try {
            QueryResult result = queryExecutionService.executeQuery(sql, 1);
            logger.info("✅ Successfully counted rows for {}.{} in {} ms", 
                       schemaName, tableName, result.getExecutionTimeMs());
            
            // Format the result in a human-readable way
            StringBuilder response = new StringBuilder();
            response.append("Row count completed successfully!\n\n");
            response.append("Table: ").append(schemaName).append(".").append(tableName).append("\n");
            response.append("Execution time: ").append(result.getExecutionTimeMs()).append(" ms\n\n");
            
            if (result.getRowCount() > 0 && result.getRows() != null && !result.getRows().isEmpty()) {
                Map<String, Object> firstRow = result.getRows().get(0);
                Object count = firstRow.get("row_count");
                response.append("Total rows: ").append(count != null ? count.toString() : "0").append("\n");
            } else {
                response.append("Total rows: 0\n");
            }
            
            return response.toString();
        } catch (SQLException e) {
            logger.error("❌ Failed to count rows for {}.{}", schemaName, tableName, e);
            return "SQL Error counting rows for '" + schemaName + "." + tableName + "': " + e.getMessage();
        }
    }

    /**
     * Test MCP connection and database connectivity
     * @return Status message indicating if MCP server and database are working
     */
    @Tool(description = "Test MCP connection and database connectivity. Returns server status and database connection information.")
    public String testConnection() {
        logger.info("🔧 MCP Tool called: testConnection()");
        try {
            QueryResult result = queryExecutionService.executeQuery("SELECT 1 as test_connection", 1);
            logger.info("✅ MCP and database connection test successful in {} ms", result.getExecutionTimeMs());
            return "MCP Query Server is operational and database connection is working. Test query executed in " + result.getExecutionTimeMs() + " ms.";
        } catch (Exception e) {
            logger.error("❌ MCP connection test failed", e);
            return "MCP connection test failed: " + e.getMessage();
        }
    }
}