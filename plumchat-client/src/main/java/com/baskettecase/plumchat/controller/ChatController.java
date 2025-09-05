package com.baskettecase.plumchat.controller;

import com.baskettecase.plumchat.model.ChatRequest;
import com.baskettecase.plumchat.model.ChatResponse;
import com.baskettecase.plumchat.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        logger.info("Received chat message: {}", request.getMessage());
        
        try {
            ChatResponse response = chatService.processMessage(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing chat message", e);
            return ResponseEntity.internalServerError()
                .body(new ChatResponse("Sorry, I encountered an error processing your message.", null, "ERROR", null));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<String>> getChatHistory(@RequestParam(required = false) String sessionId) {
        try {
            List<String> history = chatService.getChatHistory(sessionId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            logger.error("Error retrieving chat history", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
