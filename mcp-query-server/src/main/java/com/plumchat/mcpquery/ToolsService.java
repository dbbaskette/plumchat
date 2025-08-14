package com.plumchat.mcpquery;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class ToolsService {

    public record QueryResult(List<String> columns, List<List<String>> rows) {}

    private static final Logger log = LoggerFactory.getLogger(ToolsService.class);

    @Tool(description = "Execute SQL against Greenplum/PostgreSQL and return rows")
    public QueryResult execute_sql(String host, int port, String database,
                                   String username, String password,
                                   String sql,
                                   Integer fetchSize,
                                   Integer timeoutSeconds,
                                   Integer maxRows) throws Exception {
        String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        // Avoid logging plaintext credentials. If needed in debug, mask them.
        log.debug("Connecting to {} as user={} (password masked)", url, username);
        int effectiveFetch = fetchSize != null ? fetchSize : 200;
        int timeout = timeoutSeconds != null ? timeoutSeconds : 30;
        List<String> columns = new ArrayList<>();
        List<List<String>> rows = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement()) {
            conn.setNetworkTimeout(null, (int) Duration.ofSeconds(timeout).toMillis());
            stmt.setFetchSize(effectiveFetch);
            stmt.setQueryTimeout(timeout);
            if (maxRows != null && maxRows > 0) {
                stmt.setMaxRows(maxRows);
            }
            try (ResultSet rs = stmt.executeQuery(sql)) {
                log.info("Executing SQL (masked creds) for db={} host={} fetch={} timeout={} maxRows={}", database, host, effectiveFetch, timeout, maxRows);
                int columnCount = rs.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    columns.add(rs.getMetaData().getColumnLabel(i));
                }
                while (rs.next()) {
                    List<String> row = new ArrayList<>(columnCount);
                    for (int i = 1; i <= columnCount; i++) {
                        Object value = rs.getObject(i);
                        row.add(value == null ? null : String.valueOf(value));
                    }
                    rows.add(row);
                }
            }
        }
        return new QueryResult(columns, rows);
    }
}


