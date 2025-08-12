package com.baskettecase.plumchat.mcpschema;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class ToolsService {

    @Tool(description = "Health check for schema server")
    public String ping(String text) {
        return text == null ? "pong" : text;
    }
}


