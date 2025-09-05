package com.baskettecase.plumchat.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    private static final Logger logger = LoggerFactory.getLogger(ChatConfig.class);

    @Value("${plumchat.prompts.system}")
    private String systemPrompt;

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder, ToolCallbackProvider tools) {
        logger.info("Creating ChatClient with ToolCallbackProvider and externalized system prompt");
        logger.debug("System prompt loaded from properties: {}", systemPrompt.substring(0, Math.min(100, systemPrompt.length())) + "...");
        
        return chatClientBuilder
            .defaultToolCallbacks(tools)
            .defaultSystem(systemPrompt)
            .build();
    }
}
