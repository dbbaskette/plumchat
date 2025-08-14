package com.plumchat.mcpmgmt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(properties = {
        "plumchat.mgmt.enabled=false"
})
@AutoConfigureWebTestClient
class MgmtControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void exec_is_disabled_when_feature_flag_false() {
        String body = "{\"host\":\"localhost\",\"port\":22,\"user\":\"gpadmin\",\"privateKey\":\"dummy\",\"type\":\"GPSTATE\"}";
        webTestClient.post()
                .uri("/mgmt/exec")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isEqualTo(403);
    }
}


