package com.baskettecase.plumchat.mcpschema;

import jakarta.validation.constraints.NotBlank;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping(path = "/schema", produces = MediaType.APPLICATION_JSON_VALUE)
public class SchemaController {

    public record TableInfo(String schema, String name, String type) {}

    @GetMapping("/tables")
    public Mono<ResponseEntity<List<TableInfo>>> listTables(
            @RequestParam @NotBlank String host,
            @RequestParam int port,
            @RequestParam @NotBlank String database,
            @RequestParam @NotBlank String username,
            @RequestParam @NotBlank String password,
            @RequestParam(required = false) String schema,
            @RequestParam(required = false, defaultValue = "100") int limit,
            @RequestParam(required = false, defaultValue = "0") int offset) {
        return Mono.fromCallable(() -> queryTables(host, port, database, username, password, schema, limit, offset))
                .subscribeOn(Schedulers.boundedElastic())
                .map(ResponseEntity::ok);
    }

    private List<TableInfo> queryTables(String host, int port, String database, String user, String pass,
                                        String schema, int limit, int offset) throws Exception {
        String effectiveSchema = (schema == null || schema.isBlank()) ? "public" : schema;
        String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        String sql = "select table_schema, table_name, table_type from information_schema.tables " +
                "where table_schema='" + effectiveSchema + "' order by table_name limit " + limit + " offset " + offset;

        List<TableInfo> tables = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tables.add(new TableInfo(rs.getString(1), rs.getString(2), rs.getString(3)));
            }
        }
        return tables;
    }
}


