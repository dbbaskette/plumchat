package com.baskettecase.plumchat.service;

import com.baskettecase.plumchat.model.ChatRequest;
import com.baskettecase.plumchat.model.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private final ChatClient chatClient;
    private final Map<String, List<Message>> sessionHistory = new ConcurrentHashMap<>();

    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
        logger.info("ChatService initialized with Spring AI ChatClient and MCP tools auto-configuration");
    }

    public ChatResponse processMessage(ChatRequest request) {
        String sessionId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();
        String userMessage = request.getMessage();

        logger.info("Processing message for session {}: {}", sessionId, userMessage);

        try {
            // Add user message to history
            List<Message> history = sessionHistory.computeIfAbsent(sessionId, k -> new ArrayList<>());
            history.add(new UserMessage(userMessage));

            // Generate AI response using ChatClient with MCP tools
            logger.debug("Calling ChatClient with {} messages in history", history.size());
            String aiResponse = chatClient.prompt()
                    .messages(history)
                    .call()
                    .content();

            logger.info("Generated AI response using ChatClient with auto-configured MCP tools");
            logger.debug("Response: {}", aiResponse);

            // Create response with database-focused suggestions
            List<String> suggestions = generateSuggestions(userMessage, aiResponse);
            
            return new ChatResponse(aiResponse, sessionId, "SUCCESS", suggestions);

        } catch (Exception e) {
            logger.error("Error processing message", e);
            return new ChatResponse(
                "I apologize, but I encountered an error processing your request. Please try again.",
                sessionId,
                "ERROR",
                List.of("Show me all schemas", "What tables are available?")
            );
        }
    }

    public List<String> getChatHistory(String sessionId) {
        if (sessionId == null) {
            return Collections.emptyList();
        }
        
        List<Message> messages = sessionHistory.get(sessionId);
        if (messages == null) {
            return Collections.emptyList();
        }

        return messages.stream()
                .map(message -> message.getText())
                .filter(Objects::nonNull)
                .toList();
    }

    private List<String> generateSuggestions(String userMessage, String aiResponse) {
        // Generate contextual suggestions based on the conversation
        List<String> suggestions = new ArrayList<>();
        
        String lowerUserMessage = userMessage.toLowerCase();
        String lowerAiResponse = aiResponse.toLowerCase();
        
        if (lowerUserMessage.contains("schema") || lowerAiResponse.contains("schema")) {
            suggestions.add("Show me tables in a specific schema");
            suggestions.add("Get table details");
        }
        
        if (lowerUserMessage.contains("table") || lowerAiResponse.contains("table")) {
            suggestions.add("Describe table structure");
            suggestions.add("Show all schemas");
        }
        
        // Default suggestions if none match
        if (suggestions.isEmpty()) {
            suggestions.addAll(List.of(
                "Show me all database schemas",
                "What tables are available?",
                "Help me explore the database"
            ));
        }
        
        return suggestions;
    }
}
