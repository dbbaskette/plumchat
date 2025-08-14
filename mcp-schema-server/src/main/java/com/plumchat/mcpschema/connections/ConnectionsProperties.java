package com.plumchat.mcpschema.connections;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "plumchat")
public class ConnectionsProperties {

    private List<DbConnection> connections = new ArrayList<>();

    public List<DbConnection> getConnections() {
        return connections;
    }

    public void setConnections(List<DbConnection> connections) {
        this.connections = connections;
    }

    public static class DbConnection {
        private String name;
        private String host;
        private Integer port;
        private String database;
        private String roUser;
        private String roPassword;
        private String roPasswordFile;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public Integer getPort() { return port; }
        public void setPort(Integer port) { this.port = port; }
        public String getDatabase() { return database; }
        public void setDatabase(String database) { this.database = database; }
        public String getRoUser() { return roUser; }
        public void setRoUser(String roUser) { this.roUser = roUser; }
        public String getRoPassword() { return roPassword; }
        public void setRoPassword(String roPassword) { this.roPassword = roPassword; }
        public String getRoPasswordFile() { return roPasswordFile; }
        public void setRoPasswordFile(String roPasswordFile) { this.roPasswordFile = roPasswordFile; }
    }
}


