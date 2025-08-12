package com.plumchat.mcpmgmt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class McpMgmtServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(McpMgmtServerApplication.class, args);
    }
}

@RestController
class HealthController {
    @GetMapping("/healthz")
    public String health() {
        return "ok";
    }
}


