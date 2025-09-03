package com.baskettecase.mcpserver.service;

import com.baskettecase.mcpserver.model.SchemaInfo;
import com.baskettecase.mcpserver.model.TableInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver"
})
class SchemaDiscoveryServiceTest {

    private SchemaDiscoveryService schemaDiscoveryService;
    private DataSource dataSource;

    @BeforeEach
    void setUp() throws SQLException {
        // This will be injected by Spring Boot during test
        org.springframework.boot.jdbc.DataSourceBuilder<?> builder = 
            org.springframework.boot.jdbc.DataSourceBuilder.create()
                .url("jdbc:h2:mem:testdb")
                .username("sa")
                .password("")
                .driverClassName("org.h2.Driver");
        
        dataSource = builder.build();
        schemaDiscoveryService = new SchemaDiscoveryService(dataSource);
        
        // Set up test data
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            
            // Create test schema and table
            stmt.execute("DROP SCHEMA IF EXISTS test_schema CASCADE");
            stmt.execute("CREATE SCHEMA test_schema");
            stmt.execute("CREATE TABLE test_schema.customers (" +
                "id INTEGER PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(255), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");
            
            stmt.execute("CREATE TABLE test_schema.orders (" +
                "order_id INTEGER PRIMARY KEY, " +
                "customer_id INTEGER, " +
                "amount DECIMAL(10,2), " +
                "FOREIGN KEY (customer_id) REFERENCES test_schema.customers(id)" +
                ")");
        }
    }

    @Test
    void testGetAllSchemas() throws SQLException {
        List<SchemaInfo> schemas = schemaDiscoveryService.getAllSchemas();
        
        assertNotNull(schemas);
        assertFalse(schemas.isEmpty());
        
        // Find our test schema
        SchemaInfo testSchema = schemas.stream()
            .filter(s -> "TEST_SCHEMA".equals(s.schemaName()))
            .findFirst()
            .orElse(null);
            
        assertNotNull(testSchema, "Test schema should be found");
        assertEquals(2, testSchema.tableNames().size());
        assertTrue(testSchema.tableNames().contains("CUSTOMERS"));
        assertTrue(testSchema.tableNames().contains("ORDERS"));
    }

    @Test
    void testGetTablesInSchema() throws SQLException {
        List<TableInfo> tables = schemaDiscoveryService.getTablesInSchema("TEST_SCHEMA");
        
        assertNotNull(tables);
        assertEquals(2, tables.size());
        
        TableInfo customersTable = tables.stream()
            .filter(t -> "CUSTOMERS".equals(t.tableName()))
            .findFirst()
            .orElse(null);
            
        assertNotNull(customersTable);
        assertEquals("TEST_SCHEMA", customersTable.schemaName());
        assertEquals("BASE TABLE", customersTable.tableType());
        assertEquals(4, customersTable.columns().size());
    }

    @Test
    void testGetTableInfo() throws SQLException {
        TableInfo table = schemaDiscoveryService.getTableInfo("TEST_SCHEMA", "CUSTOMERS");
        
        assertNotNull(table);
        assertEquals("TEST_SCHEMA", table.schemaName());
        assertEquals("CUSTOMERS", table.tableName());
        assertEquals(4, table.columns().size());
        
        // Check for primary key
        assertTrue(table.columns().stream()
            .anyMatch(col -> "ID".equals(col.columnName()) && col.isPrimaryKey()));
    }

    @Test
    void testGetTableInfoNotFound() {
        assertThrows(IllegalArgumentException.class, () -> 
            schemaDiscoveryService.getTableInfo("TEST_SCHEMA", "NONEXISTENT_TABLE"));
    }
}