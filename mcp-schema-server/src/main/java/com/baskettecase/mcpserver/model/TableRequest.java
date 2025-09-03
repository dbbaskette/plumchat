package com.baskettecase.mcpserver.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record TableRequest(
    @JsonProperty("schemaName") String schemaName,
    @JsonProperty("tableName") String tableName
) {
    @JsonCreator
    public TableRequest {
        // Record constructor - Jackson will use this for deserialization
    }
}