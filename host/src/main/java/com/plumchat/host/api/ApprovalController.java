package com.plumchat.host.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/approval", produces = MediaType.APPLICATION_JSON_VALUE)
public class ApprovalController {

    public record ApprovalRequest(String actionId, String sql, String description, boolean approved) { }
    public record ApprovalResponse(String actionId, String status) { }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApprovalResponse> approve(@RequestBody ApprovalRequest request) {
        if (request == null || request.actionId() == null || request.actionId().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        String status = request.approved() ? "APPROVED" : "REJECTED";
        // Placeholder: future wiring to orchestrate MCP calls post-approval
        return ResponseEntity.ok(new ApprovalResponse(request.actionId(), status));
    }
}


