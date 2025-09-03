package com.baskettecase.mcpserver.integration;

import com.baskettecase.mcpserver.ToolsService;
import com.baskettecase.mcpserver.service.SchemaDiscoveryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:integrationtest;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver"
})
class SchemaToolsIntegrationTest {

    private ToolsService toolsService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        // Set up realistic test database
        org.springframework.boot.jdbc.DataSourceBuilder<?> builder = 
            org.springframework.boot.jdbc.DataSourceBuilder.create()
                .url("jdbc:h2:mem:integrationtest")
                .username("sa")
                .password("")
                .driverClassName("org.h2.Driver");
        
        DataSource dataSource = builder.build();
        SchemaDiscoveryService schemaDiscoveryService = new SchemaDiscoveryService(dataSource);
        toolsService = new ToolsService(schemaDiscoveryService);
        objectMapper = new ObjectMapper();
        
        // Create realistic test data that mimics Greenplum structure
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            
            // Clean slate
            stmt.execute("DROP ALL OBJECTS DELETE FILES");
            
            // Create schemas similar to Greenplum
            stmt.execute("CREATE SCHEMA retail");
            stmt.execute("CREATE SCHEMA analytics");
            
            // Create tables in retail schema
            stmt.execute("CREATE TABLE retail.customers (" +
                "customer_id INTEGER PRIMARY KEY, " +
                "first_name VARCHAR(100) NOT NULL, " +
                "last_name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(255) UNIQUE, " +
                "phone VARCHAR(20), " +
                "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "status VARCHAR(20) DEFAULT 'ACTIVE'" +
                ")");
                
            stmt.execute("CREATE TABLE retail.orders (" +
                "order_id INTEGER PRIMARY KEY, " +
                "customer_id INTEGER, " +
                "order_date DATE NOT NULL, " +
                "total_amount DECIMAL(10,2) NOT NULL, " +
                "status VARCHAR(20) DEFAULT 'PENDING', " +
                "FOREIGN KEY (customer_id) REFERENCES retail.customers(customer_id)" +
                ")");
            
            stmt.execute("CREATE TABLE retail.products (" +
                "product_id INTEGER PRIMARY KEY, " +
                "name VARCHAR(200) NOT NULL, " +
                "category VARCHAR(100), " +
                "price DECIMAL(10,2), " +
                "description TEXT" +
                ")");
            
            // Create tables in analytics schema
            stmt.execute("CREATE TABLE analytics.daily_sales (" +
                "sales_date DATE PRIMARY KEY, " +
                "total_orders INTEGER, " +
                "total_revenue DECIMAL(15,2), " +
                "avg_order_value DECIMAL(10,2)" +
                ")");
            
            stmt.execute("CREATE VIEW analytics.customer_summary AS " +
                "SELECT customer_id, COUNT(*) as order_count, " +
                "SUM(total_amount) as total_spent " +
                "FROM retail.orders GROUP BY customer_id");
        }
    }

    @Test
    void demonstrateGetAllSchemas() throws Exception {
        System.out.println("\n=== DEMONSTRATION: getAllSchemas Tool ===");
        String result = toolsService.getAllSchemas();
        
        assertNotNull(result);
        assertFalse(result.startsWith("Error"));
        
        // Parse and display results
        JsonNode schemas = objectMapper.readTree(result);
        System.out.println("Found " + schemas.size() + " schemas:");
        
        for (JsonNode schema : schemas) {
            String name = schema.get("schemaName").asText();
            JsonNode tables = schema.get("tableNames");
            System.out.println("  📂 " + name + " (" + tables.size() + " tables)");
            for (JsonNode table : tables) {
                System.out.println("    📄 " + table.asText());
            }
        }
        
        // Verify we found our test schemas
        boolean foundRetail = false, foundAnalytics = false;
        for (JsonNode schema : schemas) {
            String name = schema.get("schemaName").asText();
            if ("RETAIL".equals(name)) {
                foundRetail = true;
                assertEquals(3, schema.get("tableNames").size());
            } else if ("ANALYTICS".equals(name)) {
                foundAnalytics = true;
                assertEquals(2, schema.get("tableNames").size()); // 1 table + 1 view
            }
        }
        assertTrue(foundRetail && foundAnalytics, "Both test schemas should be found");
    }

    @Test 
    void demonstrateGetTablesInSchema() throws Exception {
        System.out.println("\n=== DEMONSTRATION: getTablesInSchema Tool ===");
        String result = toolsService.getTablesInSchema("RETAIL");
        
        assertNotNull(result);
        assertFalse(result.startsWith("Error"));
        
        JsonNode tables = objectMapper.readTree(result);
        System.out.println("Tables in RETAIL schema:");
        
        for (JsonNode table : tables) {
            String tableName = table.get("tableName").asText();
            String tableType = table.get("tableType").asText();
            int columnCount = table.get("columns").size();
            
            System.out.println("  📋 " + tableName + " (" + tableType + ")");
            System.out.println("    Columns: " + columnCount);
            
            // Show column details for customers table
            if ("CUSTOMERS".equals(tableName)) {
                JsonNode columns = table.get("columns");
                for (JsonNode column : columns) {
                    String colName = column.get("columnName").asText();
                    String dataType = column.get("dataType").asText();
                    boolean isPK = column.get("isPrimaryKey").asBoolean();
                    boolean nullable = column.get("nullable").asBoolean();
                    
                    String pkIndicator = isPK ? " 🔑" : "";
                    String nullIndicator = nullable ? " (nullable)" : " (required)";
                    System.out.println("      • " + colName + ": " + dataType + nullIndicator + pkIndicator);
                }
            }
        }
        
        assertEquals(3, tables.size());
    }

    @Test
    void demonstrateGetTableInfo() throws Exception {
        System.out.println("\n=== DEMONSTRATION: getTableInfo Tool ===");
        String result = toolsService.getTableInfo("RETAIL", "ORDERS");
        
        assertNotNull(result);
        assertFalse(result.startsWith("Error"));
        
        JsonNode table = objectMapper.readTree(result);
        String tableName = table.get("tableName").asText();
        JsonNode columns = table.get("columns");
        
        System.out.println("Detailed info for " + tableName + " table:");
        System.out.println("  Schema: " + table.get("schemaName").asText());
        System.out.println("  Type: " + table.get("tableType").asText());
        System.out.println("  Columns (" + columns.size() + "):");
        
        for (JsonNode column : columns) {
            String colName = column.get("columnName").asText();
            String dataType = column.get("dataType").asText();
            boolean isPK = column.get("isPrimaryKey").asBoolean();
            boolean isFK = column.get("isForeignKey").asBoolean();
            boolean nullable = column.get("nullable").asBoolean();
            
            String indicators = "";
            if (isPK) indicators += " 🔑PK";
            if (isFK) indicators += " 🔗FK";
            if (!nullable) indicators += " ⚠️NOT NULL";
            
            System.out.println("    " + colName + ": " + dataType + indicators);
        }
        
        // Verify foreign key relationship is detected
        boolean foundFK = false;
        for (JsonNode column : columns) {
            if ("CUSTOMER_ID".equals(column.get("columnName").asText())) {
                foundFK = column.get("isForeignKey").asBoolean();
                break;
            }
        }
        assertTrue(foundFK, "Foreign key should be detected");
    }
    
    @Test
    void demonstrateErrorHandling() throws Exception {
        System.out.println("\n=== DEMONSTRATION: Error Handling ===");
        
        String result1 = toolsService.getTablesInSchema("");
        assertTrue(result1.startsWith("Error: Schema name cannot be null or empty"));
        System.out.println("✓ Empty schema name handled: " + result1);
        
        String result2 = toolsService.getTableInfo("RETAIL", "NONEXISTENT");  
        assertTrue(result2.startsWith("Table not found"));
        System.out.println("✓ Non-existent table handled: " + result2);
    }
}