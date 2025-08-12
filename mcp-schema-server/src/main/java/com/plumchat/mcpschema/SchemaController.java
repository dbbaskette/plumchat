package com.baskettecase.plumchat.mcpschema;

import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/schema", produces = MediaType.APPLICATION_JSON_VALUE)
public class SchemaController {

    @GetMapping("/tables")
    public Mono<ResponseEntity<?>> listTables(
            @RequestParam @NotBlank String host,
            @RequestParam int port,
            @RequestParam @NotBlank String database,
            @RequestParam @NotBlank String username,
            @RequestParam @NotBlank String password) {
        // Placeholder: use JDBC to query information_schema
        return Mono.just(ResponseEntity.ok(new String[]{}));
    }
}


