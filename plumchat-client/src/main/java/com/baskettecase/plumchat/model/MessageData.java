package com.baskettecase.plumchat.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MessageData {
    private String type;
    private List<TableData> tables;
    private List<SchemaData> schemas;
    private String error;

    public MessageData() {}

    public MessageData(String type) {
        this.type = type;
    }

    // Getters and setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<TableData> getTables() {
        return tables;
    }

    public void setTables(List<TableData> tables) {
        this.tables = tables;
    }

    public List<SchemaData> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<SchemaData> schemas) {
        this.schemas = schemas;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public static class TableData {
        private String name;
        private String schema;
        private List<ColumnData> columns;
        private List<List<Object>> rows;

        public TableData() {}

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSchema() {
            return schema;
        }

        public void setSchema(String schema) {
            this.schema = schema;
        }

        public List<ColumnData> getColumns() {
            return columns;
        }

        public void setColumns(List<ColumnData> columns) {
            this.columns = columns;
        }

        public List<List<Object>> getRows() {
            return rows;
        }

        public void setRows(List<List<Object>> rows) {
            this.rows = rows;
        }
    }

    public static class ColumnData {
        private String name;
        private String type;
        private boolean nullable;
        @JsonProperty("primaryKey")
        private boolean primaryKey;

        public ColumnData() {}

        public ColumnData(String name, String type, boolean nullable, boolean primaryKey) {
            this.name = name;
            this.type = type;
            this.nullable = nullable;
            this.primaryKey = primaryKey;
        }

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isNullable() {
            return nullable;
        }

        public void setNullable(boolean nullable) {
            this.nullable = nullable;
        }

        public boolean isPrimaryKey() {
            return primaryKey;
        }

        public void setPrimaryKey(boolean primaryKey) {
            this.primaryKey = primaryKey;
        }
    }

    public static class SchemaData {
        private String name;
        private List<String> tables;

        public SchemaData() {}

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getTables() {
            return tables;
        }

        public void setTables(List<String> tables) {
            this.tables = tables;
        }
    }
}