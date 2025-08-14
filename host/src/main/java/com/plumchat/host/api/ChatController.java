package com.plumchat.host.api;

import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/api/chat", produces = MediaType.APPLICATION_JSON_VALUE)
public class ChatController {

    public record ChatRequest(String message) { }
    public record ChatResponse(String echo, List<String> tools) { }

    public ChatController() {}

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ChatResponse>> chat(@RequestBody ChatRequest request) {
        if (request == null || request.message() == null || request.message().isBlank()) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        // Placeholder: return available tools from query server for demo
        return Mono.just(ResponseEntity.ok(new ChatResponse("You said: " + request.message(), List.of())));
    }
}


