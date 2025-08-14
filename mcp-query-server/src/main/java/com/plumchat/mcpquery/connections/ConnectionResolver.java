package com.plumchat.mcpquery.connections;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ConnectionResolver {

    public record Resolved(String host, int port, String database, String username, String password) {}

    private final ConnectionsProperties properties;

    public ConnectionResolver(ConnectionsProperties properties) {
        this.properties = properties;
    }

    public Optional<Resolved> resolve(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        return properties.getConnections().stream()
                .filter(c -> name.equals(c.getName()))
                .findFirst()
                .map(this::toResolved);
    }

    private Resolved toResolved(ConnectionsProperties.DbConnection c) {
        String password = c.getRoPassword();
        if ((password == null || password.isBlank()) && c.getRoPasswordFile() != null) {
            try {
                password = Files.readString(Path.of(c.getRoPasswordFile())).trim();
            } catch (IOException ignored) {
                password = null;
            }
        }
        return new Resolved(c.getHost(), c.getPort() == null ? 5432 : c.getPort(), c.getDatabase(), c.getRoUser(), password);
    }
}


