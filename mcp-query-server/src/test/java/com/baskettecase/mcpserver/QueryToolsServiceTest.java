package com.baskettecase.mcpserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class QueryToolsServiceTest {

    @Autowired
    private QueryToolsService queryToolsService;

    @Test
    void testConnectionShouldWork() {
        String result = queryToolsService.testConnection();
        assertNotNull(result);
        assertTrue(result.contains("operational"));
    }

    @Test
    void testExecuteQueryWithNullSql() {
        String result = queryToolsService.executeQuery(null, null);
        assertTrue(result.contains("Error: SQL query cannot be null or empty"));
    }

    @Test
    void testExecuteQueryWithEmptySql() {
        String result = queryToolsService.executeQuery("", null);
        assertTrue(result.contains("Error: SQL query cannot be null or empty"));
    }

    @Test
    void testExecuteValidSelectQuery() {
        String result = queryToolsService.executeQuery("SELECT 1 as test_column", 10);
        assertNotNull(result);
        assertTrue(result.contains("test_column") || result.contains("\"columnNames\""));
    }

    @Test
    void testExecuteInvalidQuery() {
        String result = queryToolsService.executeQuery("INSERT INTO test VALUES (1)", null);
        assertTrue(result.contains("Only SELECT queries are allowed"));
    }

    @Test
    void testExplainQuery() {
        String result = queryToolsService.explainQuery("SELECT 1");
        assertNotNull(result);
        assertTrue(result.contains("QUERY PLAN") || result.contains("plan"));
    }

    @Test
    void testCountTableRowsWithNullSchema() {
        String result = queryToolsService.countTableRows(null, "test_table");
        assertTrue(result.contains("Error: Schema name cannot be null or empty"));
    }

    @Test
    void testCountTableRowsWithNullTable() {
        String result = queryToolsService.countTableRows("test_schema", null);
        assertTrue(result.contains("Error: Table name cannot be null or empty"));
    }
}