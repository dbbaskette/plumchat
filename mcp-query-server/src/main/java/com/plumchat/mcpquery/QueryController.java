package com.baskettecase.plumchat.mcpquery;

import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/query", produces = MediaType.APPLICATION_JSON_VALUE)
public class QueryController {

    public record QueryRequest(@NotBlank String host, int port, @NotBlank String database,
                               @NotBlank String username, @NotBlank String password,
                               @NotBlank String sql) {}
    public record QueryResponse(String[][] rows) {}

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<QueryResponse>> execute(@RequestBody QueryRequest request) {
        // Placeholder: execute SQL via JDBC and stream results
        return Mono.just(ResponseEntity.ok(new QueryResponse(new String[][]{})));
    }
}


