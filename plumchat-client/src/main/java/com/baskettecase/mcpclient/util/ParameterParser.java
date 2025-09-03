package com.baskettecase.mcpclient.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ParameterParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> parseParameters(String[] args) {
        if (args == null || args.length == 0) {
            return new HashMap<>();
        }

        // Check for JSON input
        String joinedArgs = String.join(" ", args);
        if (joinedArgs.trim().startsWith("{") && joinedArgs.trim().endsWith("}")) {
            try {
                return objectMapper.readValue(joinedArgs, new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Invalid JSON format for parameters.", e);
            }
        }

        // Parse key=value pairs
        return Arrays.stream(args)
                .map(arg -> arg.split("=", 2))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(
                        parts -> parts[0],
                        parts -> (Object) trimQuotes(parts[1]),
                        (v1, v2) -> v2, // In case of duplicate keys, last one wins
                        HashMap::new
                ));
    }

    private String trimQuotes(String value) {
        if (value == null) {
            return null;
        }
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    public String formatParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(parameters);
        } catch (JsonProcessingException e) {
            return parameters.toString();
        }
    }
}