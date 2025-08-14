package com.plumchat.mcpschema;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class ToolsService {

    public record TableInfo(String schema, String name, String type) {}

    private static final Logger log = LoggerFactory.getLogger(ToolsService.class);

    @Tool(description = "List tables and views in a schema")
    public List<TableInfo> list_tables(String host, int port, String database,
                                       String username, String password, String schema,
                                       Integer limit, Integer offset) throws Exception {
        String effectiveSchema = (schema == null || schema.isBlank()) ? "public" : schema;
        String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        log.debug("Connecting to {} as user={} (password masked)", url, username);
        String sql = "select table_schema, table_name, table_type from information_schema.tables " +
                "where table_schema='" + effectiveSchema + "' order by table_name limit " + (limit == null ? 100 : limit) +
                " offset " + (offset == null ? 0 : offset);
        List<TableInfo> tables = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            log.info("Listing tables for schema={} db={} host={} limit={} offset={}", effectiveSchema, database, host, (limit == null ? 100 : limit), (offset == null ? 0 : offset));
            while (rs.next()) {
                tables.add(new TableInfo(rs.getString(1), rs.getString(2), rs.getString(3)));
            }
        }
        return tables;
    }
}


