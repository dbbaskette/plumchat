package com.baskettecase.plumchat.mcpquery;

import jakarta.validation.constraints.NotBlank;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping(path = "/query", produces = MediaType.APPLICATION_JSON_VALUE)
public class QueryController {

    public record QueryRequest(@NotBlank String host, int port, @NotBlank String database,
                               @NotBlank String username, @NotBlank String password,
                               @NotBlank String sql,
                               Integer fetchSize,
                               Integer timeoutSeconds,
                               Integer maxRows) {}
    public record QueryResponse(List<String> columns, List<List<String>> rows) {}

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<QueryResponse>> execute(@RequestBody QueryRequest request) {
        return Mono.fromCallable(() -> runQuery(request))
                .subscribeOn(Schedulers.boundedElastic())
                .map(ResponseEntity::ok);
    }

    private QueryResponse runQuery(QueryRequest request) throws Exception {
        String url = "jdbc:postgresql://" + request.host() + ":" + request.port() + "/" + request.database();
        int fetchSize = request.fetchSize() != null ? request.fetchSize() : 200;
        int timeout = request.timeoutSeconds() != null ? request.timeoutSeconds() : 30;
        Integer maxRows = request.maxRows();
        List<String> columns = new ArrayList<>();
        List<List<String>> rows = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url, request.username(), request.password());
             Statement stmt = conn.createStatement()) {
            conn.setNetworkTimeout(null, (int) Duration.ofSeconds(timeout).toMillis());
            stmt.setFetchSize(fetchSize);
            stmt.setQueryTimeout(timeout);
            if (maxRows != null && maxRows > 0) {
                stmt.setMaxRows(maxRows);
            }
            try (ResultSet rs = stmt.executeQuery(request.sql())) {
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
        return new QueryResponse(columns, rows);
    }
}


