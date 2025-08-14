package com.plumchat.host;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
class SecuritySmokeTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void unauthenticated_access_to_protected_endpoint_is_blocked() {
        webTestClient.get().uri("/api/connections")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void login_endpoint_is_open() {
        String body = "{\"host\":\"localhost\",\"port\":5432,\"username\":\"x\",\"password\":\"y\"}";
        webTestClient.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().is4xxClientError();
    }
}


