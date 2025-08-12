package com.baskettecase.plumchat.mcpquery;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class ToolsService {

    public record QueryResult(List<String> columns, List<List<String>> rows) {}

    @Tool(description = "Execute SQL against Greenplum/PostgreSQL and return rows")
    public QueryResult execute_sql(String host, int port, String database,
                                   String username, String password,
                                   String sql,
                                   Integer fetchSize,
                                   Integer timeoutSeconds,
                                   Integer maxRows) throws Exception {
        String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
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


