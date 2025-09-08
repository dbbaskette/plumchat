package com.baskettecase.mcpserver.service;

import com.baskettecase.mcpserver.model.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Service
public class QueryExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(QueryExecutionService.class);
    private static final int DEFAULT_MAX_ROWS = 1000;
    private static final int ABSOLUTE_MAX_ROWS = 10000;

    private final DataSource dataSource;

    public QueryExecutionService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public QueryResult executeQuery(String sql, Integer maxRows) throws SQLException {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL query cannot be null or empty");
        }

        String trimmedSql = sql.trim();
        if (!isSelectQuery(trimmedSql)) {
            throw new IllegalArgumentException("Only SELECT queries are allowed for security reasons");
        }

        int effectiveMaxRows = determineMaxRows(maxRows);
        long startTime = System.currentTimeMillis();

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            statement.setMaxRows(effectiveMaxRows);
            
            logger.info("Executing query with max rows {}: {}", effectiveMaxRows, trimmedSql.substring(0, Math.min(100, trimmedSql.length())));

            try (ResultSet resultSet = statement.executeQuery(trimmedSql)) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                List<String> columnNames = new ArrayList<>();
                List<Map<String, Object>> columnMetadata = new ArrayList<>();
                
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    columnNames.add(columnName);
                    
                    // Collect column metadata
                    Map<String, Object> colMeta = new LinkedHashMap<>();
                    colMeta.put("name", columnName);
                    colMeta.put("type", metaData.getColumnTypeName(i));
                    colMeta.put("nullable", metaData.isNullable(i) != ResultSetMetaData.columnNoNulls);
                    colMeta.put("precision", metaData.getPrecision(i));
                    colMeta.put("scale", metaData.getScale(i));
                    
                    columnMetadata.add(colMeta);
                }

                List<Map<String, Object>> rows = new ArrayList<>();
                int rowCount = 0;

                while (resultSet.next() && rowCount < effectiveMaxRows) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = resultSet.getObject(i);
                        row.put(columnName, value);
                    }
                    rows.add(row);
                    rowCount++;
                }

                long executionTime = System.currentTimeMillis() - startTime;
                logger.info("Query executed successfully. Returned {} rows in {} ms", rowCount, executionTime);

                QueryResult queryResult = new QueryResult(columnNames, rows, rowCount, executionTime);
                queryResult.setColumnMetadata(columnMetadata);
                return queryResult;
            }
        } catch (SQLException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Query execution failed after {} ms: {}", executionTime, e.getMessage());
            throw e;
        }
    }

    public QueryResult explainQuery(String sql) throws SQLException {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL query cannot be null or empty");
        }

        String trimmedSql = sql.trim();
        if (!isSelectQuery(trimmedSql)) {
            throw new IllegalArgumentException("Only SELECT queries can be explained");
        }

        String explainSql = "EXPLAIN " + trimmedSql;
        long startTime = System.currentTimeMillis();

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(explainSql)) {

            List<String> columnNames = Arrays.asList("QUERY PLAN");
            List<Map<String, Object>> rows = new ArrayList<>();

            while (resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("QUERY PLAN", resultSet.getString(1));
                rows.add(row);
            }

            long executionTime = System.currentTimeMillis() - startTime;
            logger.info("Query plan generated in {} ms", executionTime);

            return new QueryResult(columnNames, rows, rows.size(), executionTime);
        } catch (SQLException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Query explain failed after {} ms: {}", executionTime, e.getMessage());
            throw e;
        }
    }

    private boolean isSelectQuery(String sql) {
        String upperCaseSql = sql.toUpperCase().trim();
        return upperCaseSql.startsWith("SELECT") || upperCaseSql.startsWith("WITH");
    }

    private int determineMaxRows(Integer maxRows) {
        if (maxRows == null) {
            return DEFAULT_MAX_ROWS;
        }
        if (maxRows <= 0) {
            return DEFAULT_MAX_ROWS;
        }
        return Math.min(maxRows, ABSOLUTE_MAX_ROWS);
    }
}