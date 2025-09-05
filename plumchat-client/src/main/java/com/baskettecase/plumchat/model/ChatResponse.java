package com.baskettecase.plumchat.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ChatResponse {
    private ChatMessage message;
    private String sessionId;
    private String status;
    private List<String> suggestions;

    public ChatResponse() {}

    public ChatResponse(String content, String sessionId, String status, List<String> suggestions) {
        this.message = new ChatMessage(
            UUID.randomUUID().toString(),
            content,
            "ASSISTANT",
            LocalDateTime.now(),
            null
        );
        this.sessionId = sessionId != null ? sessionId : UUID.randomUUID().toString();
        this.status = status != null ? status : "SUCCESS";
        this.suggestions = suggestions != null ? suggestions : List.of(
            "Show me all schemas",
            "What tables are available?"
        );
    }

    // Getters and setters
    public ChatMessage getMessage() {
        return message;
    }

    public void setMessage(ChatMessage message) {
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public static class ChatMessage {
        private String id;
        private String content;
        private String role;
        private LocalDateTime timestamp;
        private Object data;

        public ChatMessage() {}

        public ChatMessage(String id, String content, String role, LocalDateTime timestamp, Object data) {
            this.id = id;
            this.content = content;
            this.role = role;
            this.timestamp = timestamp;
            this.data = data;
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }
}
