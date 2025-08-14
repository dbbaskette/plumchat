package com.plumchat.host.api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    public record LoginRequest(String host, Integer port, String database, String username, String password) {}
    public record LoginResponse(String status, String message) {}
    public record DatabasesResponse(List<String> databases) {}

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        if (!StringUtils.hasText(req.host()) || req.port() == null || !StringUtils.hasText(req.username()) || !StringUtils.hasText(req.password())) {
            return ResponseEntity.badRequest().body(new LoginResponse("ERROR", "Missing required fields"));
        }
        String url = "jdbc:postgresql://" + req.host() + ":" + req.port() + "/" + (StringUtils.hasText(req.database()) ? req.database() : "postgres");
        try (Connection conn = DriverManager.getConnection(url, req.username(), req.password())) {
            ResponseCookie sessionCookie = ResponseCookie.from("pc_db_session", "ok").httpOnly(true).secure(true).sameSite("Strict").path("/").build();
            return ResponseEntity.ok().header("Set-Cookie", sessionCookie.toString()).body(new LoginResponse("OK", "Authenticated"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new LoginResponse("ERROR", "Authentication failed"));
        }
    }

    @PostMapping(path = "/databases", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatabasesResponse> listDatabases(@RequestBody LoginRequest req) {
        if (!StringUtils.hasText(req.host()) || req.port() == null || !StringUtils.hasText(req.username()) || !StringUtils.hasText(req.password())) {
            return ResponseEntity.badRequest().build();
        }
        String url = "jdbc:postgresql://" + req.host() + ":" + req.port() + "/postgres";
        List<String> dbs = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, req.username(), req.password());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select datname from pg_database where datallowconn and datistemplate = false order by datname")) {
            while (rs.next()) {
                dbs.add(rs.getString(1));
            }
            return ResponseEntity.ok(new DatabasesResponse(dbs));
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }
}


