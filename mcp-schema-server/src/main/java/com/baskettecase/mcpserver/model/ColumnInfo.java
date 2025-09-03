package com.baskettecase.mcpserver.model;

public record ColumnInfo(
    String columnName,
    String dataType,
    Integer columnSize,
    Integer decimalDigits,
    boolean nullable,
    String defaultValue,
    String remarks,
    boolean isPrimaryKey,
    boolean isForeignKey
) {}