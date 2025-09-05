package com.baskettecase.mcpserver.model;

public class QueryRequest {
    private String sql;
    private Integer maxRows;

    public QueryRequest() {
    }

    public QueryRequest(String sql, Integer maxRows) {
        this.sql = sql;
        this.maxRows = maxRows;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Integer getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(Integer maxRows) {
        this.maxRows = maxRows;
    }
}