package com.baskettecase.mcpserver.model;

import java.util.List;

public record SchemaInfo(
    String schemaName,
    String owner,
    List<String> tableNames
) {}