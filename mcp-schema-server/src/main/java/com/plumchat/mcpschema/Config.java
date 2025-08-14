package com.plumchat.mcpschema;

import com.plumchat.mcpschema.connections.ConnectionsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ConnectionsProperties.class)
public class Config {}


