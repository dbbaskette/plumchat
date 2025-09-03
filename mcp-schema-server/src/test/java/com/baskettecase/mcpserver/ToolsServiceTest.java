package com.baskettecase.mcpserver;

import com.baskettecase.mcpserver.model.SchemaRequest;
import com.baskettecase.mcpserver.model.TableRequest;
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
    "spring.datasource.url=jdbc:h2:mem:testdb2;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver"
})
class ToolsServiceTest {

    private ToolsService toolsService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        // Set up in-memory database
        org.springframework.boot.jdbc.DataSourceBuilder<?> builder = 
            org.springframework.boot.jdbc.DataSourceBuilder.create()
                .url("jdbc:h2:mem:testdb2")
                .username("sa")
                .password("")
                .driverClassName("org.h2.Driver");
        
        DataSource dataSource = builder.build();
        SchemaDiscoveryService schemaDiscoveryService = new SchemaDiscoveryService(dataSource);
        toolsService = new ToolsService(schemaDiscoveryService);
        objectMapper = new ObjectMapper();
        
        // Create test data
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            
            stmt.execute("DROP SCHEMA IF EXISTS sales CASCADE");
            stmt.execute("CREATE SCHEMA sales");
            stmt.execute("CREATE TABLE sales.products (" +
                "product_id INTEGER PRIMARY KEY, " +
                "product_name VARCHAR(200) NOT NULL, " +
                "price DECIMAL(10,2)" +
                ")");
        }
    }

    @Test
    void testGetAllSchemas() throws Exception {
        String result = toolsService.getAllSchemas();
        
        assertNotNull(result);
        assertFalse(result.startsWith("Error"));
        
        JsonNode jsonNode = objectMapper.readTree(result);
        assertTrue(jsonNode.isArray());
        assertTrue(jsonNode.size() > 0);
        
        // Find our sales schema
        boolean foundSalesSchema = false;
        for (JsonNode schema : jsonNode) {
            if ("SALES".equals(schema.get("schemaName").asText())) {
                foundSalesSchema = true;
                assertTrue(schema.get("tableNames").isArray());
                assertEquals("PRODUCTS", schema.get("tableNames").get(0).asText());
                break;
            }
        }
        assertTrue(foundSalesSchema, "Sales schema should be found");
    }

    @Test
    void testGetTablesInSchema() throws Exception {
        String result = toolsService.getTablesInSchema(new SchemaRequest("SALES"));
        
        assertNotNull(result);
        assertFalse(result.startsWith("Error"));
        
        JsonNode jsonNode = objectMapper.readTree(result);
        assertTrue(jsonNode.isArray());
        assertEquals(1, jsonNode.size());
        
        JsonNode table = jsonNode.get(0);
        assertEquals("SALES", table.get("schemaName").asText());
        assertEquals("PRODUCTS", table.get("tableName").asText());
        assertEquals("BASE TABLE", table.get("tableType").asText());
        assertTrue(table.get("columns").isArray());
        assertEquals(3, table.get("columns").size());
    }

    @Test
    void testGetTableInfo() throws Exception {
        String result = toolsService.getTableInfo(new TableRequest("SALES", "PRODUCTS"));
        
        assertNotNull(result);
        assertFalse(result.startsWith("Error"));
        
        JsonNode jsonNode = objectMapper.readTree(result);
        assertEquals("SALES", jsonNode.get("schemaName").asText());
        assertEquals("PRODUCTS", jsonNode.get("tableName").asText());
        assertTrue(jsonNode.get("columns").isArray());
        
        // Check primary key column
        JsonNode columns = jsonNode.get("columns");
        boolean foundPrimaryKey = false;
        for (JsonNode column : columns) {
            if ("PRODUCT_ID".equals(column.get("columnName").asText())) {
                assertTrue(column.get("isPrimaryKey").asBoolean());
                foundPrimaryKey = true;
                break;
            }
        }
        assertTrue(foundPrimaryKey, "Primary key should be found");
    }

    @Test
    void testGetTablesInSchemaEmptyName() {
        String result = toolsService.getTablesInSchema(new SchemaRequest(""));
        assertTrue(result.startsWith("Error: Schema name cannot be null or empty"));
        
        result = toolsService.getTablesInSchema(new SchemaRequest(null));
        assertTrue(result.startsWith("Error: Schema name cannot be null or empty"));
    }

    @Test
    void testGetTableInfoEmptyNames() {
        String result = toolsService.getTableInfo(new TableRequest("", "products"));
        assertTrue(result.startsWith("Error: Schema name cannot be null or empty"));
        
        result = toolsService.getTableInfo(new TableRequest("sales", ""));
        assertTrue(result.startsWith("Error: Table name cannot be null or empty"));
    }

    @Test
    void testGetTableInfoNotFound() {
        String result = toolsService.getTableInfo(new TableRequest("SALES", "NONEXISTENT"));
        assertTrue(result.startsWith("Table not found: SALES.NONEXISTENT"));
    }
}