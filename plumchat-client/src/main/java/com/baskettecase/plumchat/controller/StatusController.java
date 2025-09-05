package com.baskettecase.plumchat.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.baskettecase.plumchat.service.ChatService;

import java.util.Map;

@RestController
@RequestMapping("/api/status")
@CrossOrigin(origins = "*")
public class StatusController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ChatService chatService;
    
    @Value("${spring.ai.openai.chat.options.model:gpt-4o-mini}")
    private String openAiModel;

    public StatusController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/health")
    public Map<String, Object> getHealth() {
        return Map.of(
            "status", "UP",
            "service", "PlumChat Client",
            "timestamp", System.currentTimeMillis()
        );
    }

    @GetMapping("/connections")
    public Map<String, Object> getConnectionStatus() {
        return Map.of(
            "schema", checkMcpServerStatus("schema-server", "http://localhost:8080"),
            "query", checkMcpServerStatus("query-server", "http://localhost:8081"),
            "mgmt", Map.of(
                "connected", false,
                "status", "Not Implemented", 
                "note", "Management server not yet implemented"
            )
        );
    }

    @GetMapping("/llm")
    public Map<String, Object> getLlmStatus() {
        // Check if OpenAI API key is configured
        String apiKey = System.getenv("OPENAI_API_KEY");
        boolean hasApiKey = apiKey != null && !apiKey.isEmpty() && !"your-openai-api-key-here".equals(apiKey);
        
        return Map.of(
            "connected", hasApiKey,
            "healthy", hasApiKey,
            "model", openAiModel,
            "hasApiKey", hasApiKey,
            "hasModel", true,
            "hasChatClient", true
        );
    }

    @GetMapping("/database")
    public Map<String, Object> getDatabaseStatus() {
        // This would typically check database connectivity
        // For now, we'll indicate it's accessible via MCP
        return Map.of(
            "connected", true,
            "status", "Available via MCP",
            "note", "Database access through MCP Schema Server"
        );
    }

    @GetMapping("/system")
    public Map<String, Object> getSystemStatus() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        return Map.of(
            "status", "UP",
            "uptime", System.currentTimeMillis(),
            "memory", Map.of(
                "used", usedMemory / (1024 * 1024) + " MB",
                "free", freeMemory / (1024 * 1024) + " MB",
                "total", totalMemory / (1024 * 1024) + " MB",
                "max", maxMemory / (1024 * 1024) + " MB"
            ),
            "jvm", Map.of(
                "version", System.getProperty("java.version"),
                "vendor", System.getProperty("java.vendor")
            )
        );
    }

    private Map<String, Object> checkMcpServerStatus(String serverName, String serverUrl) {
        try {
            // Try to check the actuator health endpoint
            restTemplate.getForObject(serverUrl + "/actuator/health", String.class);
            return Map.of(
                "connected", true,
                "status", "Online",
                "note", "MCP server responding"
            );
        } catch (Exception e) {
            return Map.of(
                "connected", false,
                "status", "Offline",
                "note", "Cannot connect to MCP server at " + serverUrl
            );
        }
    }

    @GetMapping("/welcome")
    public Map<String, Object> getWelcomeMessage() {
        return Map.of(
            "message", chatService.getWelcomeMessage(),
            "source", "externalized-property"
        );
    }
}
