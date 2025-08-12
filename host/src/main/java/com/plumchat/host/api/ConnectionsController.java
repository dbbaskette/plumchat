package com.baskettecase.plumchat.host.api;

import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/connections", produces = MediaType.APPLICATION_JSON_VALUE)
public class ConnectionsController {

    public record ConnectionSummary(String name, String database, List<String> capabilities) {}

    @GetMapping
    public ResponseEntity<List<ConnectionSummary>> list() {
        // Placeholder: later read from shared config or forward to servers. No secrets returned.
        return ResponseEntity.ok(List.of());
    }
}


