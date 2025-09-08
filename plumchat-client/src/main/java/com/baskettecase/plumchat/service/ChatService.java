package com.baskettecase.plumchat.service;

import com.baskettecase.plumchat.model.ChatRequest;
import com.baskettecase.plumchat.model.ChatResponse;
import com.baskettecase.plumchat.model.MessageData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final Map<String, List<Message>> sessionHistory = new ConcurrentHashMap<>();

    @Value("${plumchat.prompts.error}")
    private String errorPrompt;

    @Value("${plumchat.prompts.welcome}")
    private String welcomePrompt;

    public ChatService(ChatClient chatClient, ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
        logger.info("ChatService initialized with Spring AI ChatClient and externalized prompts");
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
            logger.debug("Response length: {} characters", aiResponse.length());

            // Parse structured data from the AI response
            MessageData structuredData = parseStructuredData(aiResponse);
            
            // Create response with database-focused suggestions
            List<String> suggestions = generateSuggestions(userMessage, aiResponse);
            
            return createChatResponse(aiResponse, sessionId, "SUCCESS", suggestions, structuredData);

        } catch (Exception e) {
            logger.error("Error processing message", e);
            return new ChatResponse(
                errorPrompt,
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

    public String getWelcomeMessage() {
        return welcomePrompt;
    }

    private MessageData parseStructuredData(String aiResponse) {
        try {
            logger.debug("Parsing structured data from response of length: {}", aiResponse.length());
            
            // First, try to find JSON blocks (original approach)
            List<String> jsonCandidates = extractJsonBlocks(aiResponse);
            logger.debug("Found {} JSON candidates", jsonCandidates.size());
            
            for (int i = 0; i < jsonCandidates.size(); i++) {
                String jsonStr = jsonCandidates.get(i);
                logger.debug("Candidate {}: {} characters starting with: {}", 
                    i, jsonStr.length(), jsonStr.substring(0, Math.min(200, jsonStr.length())));
                
                try {
                    JsonNode jsonNode = objectMapper.readTree(jsonStr);
                    if (jsonNode.has("type") && "query_result".equals(jsonNode.get("type").asText())) {
                        logger.info("Found query_result JSON, parsing table data...");
                        return parseQueryResult(jsonNode);
                    }
                } catch (JsonProcessingException e) {
                    logger.debug("Failed to parse JSON candidate {}: {}", i, e.getMessage());
                }
            }
            
            // If no JSON found, try parsing markdown tables
            MessageData markdownData = parseMarkdownTable(aiResponse);
            if (markdownData != null) {
                logger.info("Successfully parsed markdown table data");
                return markdownData;
            }
            
            logger.warn("No structured data found in AI response");
        } catch (Exception e) {
            logger.warn("Error parsing structured data from AI response", e);
        }
        return null;
    }

    private List<String> extractJsonBlocks(String text) {
        List<String> jsonBlocks = new ArrayList<>();
        int start = -1;
        int braceCount = 0;
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            
            if (c == '{') {
                if (braceCount == 0) {
                    start = i;
                }
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0 && start != -1) {
                    String candidate = text.substring(start, i + 1);
                    if (candidate.contains("\"type\"")) {
                        jsonBlocks.add(candidate);
                    }
                    start = -1;
                }
            }
        }
        
        return jsonBlocks;
    }

    private MessageData parseQueryResult(JsonNode jsonNode) {
        MessageData messageData = new MessageData("query_result");
        
        try {
            if (jsonNode.has("columnNames")) {
                List<String> columnNames = objectMapper.convertValue(
                    jsonNode.get("columnNames"), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );
                
                // Create table data
                MessageData.TableData tableData = new MessageData.TableData();
                tableData.setName("query_result");
                tableData.setSchema("default");
                
                // Convert column names to ColumnData objects with metadata if available
                List<MessageData.ColumnData> columns = new ArrayList<>();
                
                if (jsonNode.has("columnMetadata")) {
                    List<Map<String, Object>> columnMetadata = objectMapper.convertValue(
                        jsonNode.get("columnMetadata"),
                        objectMapper.getTypeFactory().constructCollectionType(
                            List.class,
                            objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)
                        )
                    );
                    
                    // Use metadata to create richer column information
                    for (Map<String, Object> meta : columnMetadata) {
                        String name = (String) meta.get("name");
                        String type = (String) meta.get("type");
                        Boolean nullable = (Boolean) meta.get("nullable");
                        columns.add(new MessageData.ColumnData(
                            name, 
                            type != null ? type : "TEXT", 
                            nullable != null ? nullable : true, 
                            false // We don't have PK info from query results
                        ));
                    }
                } else {
                    // Fallback to simple column names
                    columns = columnNames.stream()
                        .map(name -> new MessageData.ColumnData(name, "TEXT", true, false))
                        .toList();
                }
                
                tableData.setColumns(columns);
                
                // Add rows if available
                if (jsonNode.has("rows")) {
                    List<List<Object>> rows = objectMapper.convertValue(
                        jsonNode.get("rows"),
                        objectMapper.getTypeFactory().constructCollectionType(
                            List.class, 
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Object.class)
                        )
                    );
                    tableData.setRows(rows);
                }
                
                messageData.setTables(List.of(tableData));
            }
        } catch (Exception e) {
            logger.error("Error parsing query result JSON", e);
        }
        
        return messageData;
    }

    private MessageData parseMarkdownTable(String aiResponse) {
        try {
            // Look for markdown table patterns
            String[] lines = aiResponse.split("\n");
            List<String> tableLines = new ArrayList<>();
            boolean inTable = false;
            
            for (String line : lines) {
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith("|") && trimmedLine.endsWith("|")) {
                    if (!inTable) {
                        inTable = true;
                    }
                    tableLines.add(trimmedLine);
                } else if (inTable && trimmedLine.contains("---")) {
                    // This is the separator line, include it
                    tableLines.add(trimmedLine);
                } else if (inTable && !trimmedLine.isEmpty()) {
                    // End of table
                    break;
                }
            }
            
            if (tableLines.size() < 3) { // Need at least header, separator, and one data row
                logger.debug("Not enough table lines found: {}", tableLines.size());
                return null;
            }
            
            // Parse header row
            String headerLine = tableLines.get(0);
            List<String> columnNames = parseTableRow(headerLine);
            
            if (columnNames.isEmpty()) {
                logger.debug("No column names found in header");
                return null;
            }
            
            logger.debug("Found markdown table with columns: {}", String.join(", ", columnNames));
            
            // Parse data rows (skip header and separator)
            List<List<Object>> rows = new ArrayList<>();
            for (int i = 2; i < tableLines.size(); i++) {
                List<String> rowData = parseTableRow(tableLines.get(i));
                if (rowData.size() == columnNames.size()) {
                    rows.add(new ArrayList<>(rowData));
                }
            }
            
            // Create MessageData structure
            MessageData messageData = new MessageData("query_result");
            MessageData.TableData tableData = new MessageData.TableData();
            tableData.setName("query_result");
            tableData.setSchema("default");
            
            // Create columns with basic metadata
            List<MessageData.ColumnData> columns = columnNames.stream()
                .map(name -> new MessageData.ColumnData(name, "TEXT", true, false))
                .toList();
            
            tableData.setColumns(columns);
            tableData.setRows(rows);
            messageData.setTables(List.of(tableData));
            
            logger.info("Successfully parsed markdown table: {} columns, {} rows", 
                columnNames.size(), rows.size());
            
            return messageData;
            
        } catch (Exception e) {
            logger.warn("Error parsing markdown table", e);
        }
        return null;
    }

    private List<String> parseTableRow(String line) {
        List<String> cells = new ArrayList<>();
        if (line.startsWith("|")) {
            line = line.substring(1);
        }
        if (line.endsWith("|")) {
            line = line.substring(0, line.length() - 1);
        }
        
        String[] parts = line.split("\\|");
        for (String part : parts) {
            cells.add(part.trim());
        }
        
        return cells;
    }

    private ChatResponse createChatResponse(String content, String sessionId, String status, 
                                          List<String> suggestions, MessageData structuredData) {
        ChatResponse response = new ChatResponse(content, sessionId, status, suggestions);
        if (structuredData != null) {
            response.getMessage().setData(structuredData);
        }
        return response;
    }
}
