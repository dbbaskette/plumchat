package com.baskettecase.plumchat.mcpmgmt;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/mgmt", produces = MediaType.APPLICATION_JSON_VALUE)
public class MgmtController {

    public record CommandRequest(String host, int port, String user, String privateKey, String command) {}
    public record CommandResponse(String status, String output) {}

    @PostMapping(path = "/exec", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<CommandResponse>> exec(@RequestBody CommandRequest request) {
        // Placeholder: run SSH command with guardrails
        return Mono.just(ResponseEntity.ok(new CommandResponse("OK", "")));
    }
}


