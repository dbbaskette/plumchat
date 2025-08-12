package com.baskettecase.plumchat.mcpschema;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@ComponentScan(basePackages = "com.baskettecase")
public class McpSchemaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(McpSchemaServerApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider toolsProvider(ToolsService toolsService) {
        return MethodToolCallbackProvider.builder().toolObjects(toolsService).build();
    }
}

@RestController
class HealthController {
    @GetMapping("/healthz")
    public String health() {
        return "ok";
    }
}


