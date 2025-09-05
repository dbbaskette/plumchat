package com.baskettecase.mcpserver;

import com.baskettecase.mcpserver.QueryToolsService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.baskettecase")
public class McpServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpServerApplication.class, args);
	}

	@Bean
	public ToolCallbackProvider toolsProvider(QueryToolsService queryToolsService) {
		return MethodToolCallbackProvider.builder().toolObjects(queryToolsService).build();
	}

}