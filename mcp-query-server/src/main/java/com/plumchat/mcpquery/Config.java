package com.plumchat.mcpquery;

import com.plumchat.mcpquery.connections.ConnectionsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ConnectionsProperties.class)
public class Config {}


