package com.baskettecase.plumchat.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    private static final Logger logger = LoggerFactory.getLogger(ChatConfig.class);

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder, ToolCallbackProvider tools) {
        logger.info("Creating ChatClient with ToolCallbackProvider - exact Spring AI example pattern");
        
        return chatClientBuilder
            .defaultToolCallbacks(tools)
            .defaultSystem("""
                You are PlumChat, an AI assistant specialized in Greenplum database operations.
                
                Your primary capabilities include:
                - Accessing live database schema information using available tools
                - Exploring database schemas, tables, and structures
                - Providing guidance on database operations and SQL queries
                - Helping with Greenplum best practices
                
                IMPORTANT: When users ask about database schemas, tables, or structure:
                1. ALWAYS use the available database tools to get real, current information
                2. Call the appropriate tools to retrieve actual data rather than providing generic responses
                3. If users ask about schemas, use getAllSchemas tool
                4. If users ask about tables in a schema, use getTablesInSchema tool
                5. If users ask about table details, use getTableInfo tool
                
                Be helpful, accurate, and always use the tools available to provide real database information.
                """)
            .build();
    }
}
