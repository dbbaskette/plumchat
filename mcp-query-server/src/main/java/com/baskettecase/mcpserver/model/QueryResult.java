package com.baskettecase.mcpserver.model;

import java.util.List;
import java.util.Map;

public class QueryResult {
    private List<String> columnNames;
    private List<Map<String, Object>> columnMetadata;
    private List<Map<String, Object>> rows;
    private int rowCount;
    private long executionTimeMs;
    private String message;

    public QueryResult() {
    }

    public QueryResult(List<String> columnNames, List<Map<String, Object>> rows, int rowCount, long executionTimeMs) {
        this.columnNames = columnNames;
        this.rows = rows;
        this.rowCount = rowCount;
        this.executionTimeMs = executionTimeMs;
    }

    public QueryResult(String message) {
        this.message = message;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public void setRows(List<Map<String, Object>> rows) {
        this.rows = rows;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Map<String, Object>> getColumnMetadata() {
        return columnMetadata;
    }

    public void setColumnMetadata(List<Map<String, Object>> columnMetadata) {
        this.columnMetadata = columnMetadata;
    }
}