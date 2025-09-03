package com.baskettecase.mcpclient.config;

import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

@Service
public class McpConnectionConfigService {

    private static final Path RESOURCES_PATH = Paths.get("src", "main", "resources");

    public boolean sseConnectionExists(String serverName) throws IOException {
        return connectionExists("sse", "spring.ai.mcp.client.sse.connections." + serverName + ".url");
    }

    public boolean stdioConnectionExists(String serverName) throws IOException {
        return connectionExists("stdio", "spring.ai.mcp.client.stdio.connections." + serverName + ".command");
    }

    private boolean connectionExists(String profile, String key) throws IOException {
        Path propertiesPath = RESOURCES_PATH.resolve("application-" + profile + ".properties");
        if (!Files.exists(propertiesPath)) {
            return false;
        }
        Properties properties = new Properties();
        try (FileReader reader = new FileReader(propertiesPath.toFile())) {
            properties.load(reader);
            return properties.containsKey(key);
        }
    }

    public void addSseConnection(String serverName, String url) throws IOException {
        addSseConnection(serverName, url, false);
    }

    public void addSseConnection(String serverName, String url, boolean skipSslValidation) throws IOException {
        addSseConnection(serverName, url, "/sse", null, skipSslValidation);
    }

    public void addSseConnection(String serverName, String url, String sseEndpoint, String authHeader, boolean skipSslValidation) throws IOException {
        Path propertiesPath = RESOURCES_PATH.resolve("application-sse.properties");
        if (!Files.exists(propertiesPath)) {
            Files.createFile(propertiesPath);
        }
        
        StringBuilder configBuilder = new StringBuilder();
        configBuilder.append(String.format("\n# Connection for %s\n", serverName));
        configBuilder.append(String.format("spring.ai.mcp.client.sse.connections.%s.url=%s\n", serverName, url));
        configBuilder.append(String.format("spring.ai.mcp.client.sse.connections.%s.sse-endpoint=%s\n", serverName, sseEndpoint));
        
        // Add authorization header if provided
        if (authHeader != null && !authHeader.trim().isEmpty()) {
            configBuilder.append(String.format("spring.ai.mcp.client.sse.connections.%s.headers.Authorization=%s\n", serverName, authHeader));
        }
        
        // Add SSL skip validation if requested
        if (skipSslValidation) {
            configBuilder.append(String.format("spring.ai.mcp.client.sse.connections.%s.skip-ssl-validation=true\n", serverName));
        }
        
        Files.writeString(propertiesPath, configBuilder.toString(), StandardOpenOption.APPEND);
    }

    public void addStdioConnection(String serverName, String jarPath) throws IOException {
        addStdioConnection(serverName, jarPath, null);
    }

    public void addStdioConnection(String serverName, String jarPath, String profile) throws IOException {
        Path propertiesPath = RESOURCES_PATH.resolve("application-stdio.properties");
        if (!Files.exists(propertiesPath)) {
            Files.createFile(propertiesPath);
        }
        
        // Use provided profile or default to "stdio" if none specified
        String springProfile = (profile != null && !profile.trim().isEmpty()) ? profile : "stdio";
        
        String config = String.format(
                "\n# Connection for %s\n" +
                "spring.ai.mcp.client.stdio.connections.%s.command=java\n" +
                "spring.ai.mcp.client.stdio.connections.%s.args=-Dspring.profiles.active=%s,-jar,%s\n",
                serverName, serverName, serverName, springProfile, jarPath);
        Files.writeString(propertiesPath, config, StandardOpenOption.APPEND);
    }

    public void addStdioNativeConnection(String serverName, String command, String[] args) throws IOException {
        Path propertiesPath = RESOURCES_PATH.resolve("application-stdio.properties");
        if (!Files.exists(propertiesPath)) {
            Files.createFile(propertiesPath);
        }

        String argsString = String.join(",", args);

        String config = String.format(
                "\n# Connection for %s (native executable)\n" +
                "spring.ai.mcp.client.stdio.connections.%s.command=%s\n" +
                "spring.ai.mcp.client.stdio.connections.%s.args=%s\n",
                serverName, serverName, command, serverName, argsString);
        Files.writeString(propertiesPath, config, StandardOpenOption.APPEND);
    }
}