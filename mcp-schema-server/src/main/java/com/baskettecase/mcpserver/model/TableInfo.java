package com.baskettecase.mcpserver.model;

import java.util.List;

public record TableInfo(
    String schemaName,
    String tableName,
    String tableType,
    String remarks,
    List<ColumnInfo> columns
) {}