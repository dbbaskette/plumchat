package com.baskettecase.plumchat.host.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/chat", produces = MediaType.APPLICATION_JSON_VALUE)
public class ChatController {

    public record ChatRequest(String message) { }
    public record ChatResponse(String echo) { }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        if (request == null || request.message() == null || request.message().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        // Placeholder: later will orchestrate MCP servers and approval flow
        return ResponseEntity.ok(new ChatResponse("You said: " + request.message()));
    }
}


