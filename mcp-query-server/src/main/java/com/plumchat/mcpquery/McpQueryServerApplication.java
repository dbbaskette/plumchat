package com.baskettecase.plumchat.mcpquery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class McpQueryServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(McpQueryServerApplication.class, args);
    }
}

@RestController
class HealthController {
    @GetMapping("/healthz")
    public String health() {
        return "ok";
    }
}


