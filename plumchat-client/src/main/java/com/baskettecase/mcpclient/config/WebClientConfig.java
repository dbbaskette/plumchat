package com.baskettecase.mcpclient.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${OPENMETADATA_PAT:}")
    private String openMetadataToken;

    @Bean
    @Primary
    public WebClient.Builder webClientBuilder() {
        WebClient.Builder builder = WebClient.builder();
        
        // Add default authorization header if OpenMetadata token is available
        if (openMetadataToken != null && !openMetadataToken.trim().isEmpty()) {
            builder.defaultHeader("Authorization", "Bearer " + openMetadataToken);
        }
        
        return builder;
    }
}