package com.baskettecase.mcpserver.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record SchemaRequest(
    @JsonProperty("schemaName") String schemaName
) {
    @JsonCreator
    public SchemaRequest {
        // Record constructor - Jackson will use this for deserialization
    }
}