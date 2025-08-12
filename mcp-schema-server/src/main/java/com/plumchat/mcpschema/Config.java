package com.baskettecase.plumchat.mcpschema;

import com.baskettecase.plumchat.mcpschema.connections.ConnectionsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ConnectionsProperties.class)
public class Config {}


