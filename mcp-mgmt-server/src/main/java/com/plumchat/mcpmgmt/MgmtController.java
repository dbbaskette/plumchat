package com.plumchat.mcpmgmt;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
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
@RequestMapping(path = "/mgmt", produces = MediaType.APPLICATION_JSON_VALUE)
public class MgmtController {

    public enum CommandType { GPSTART, GPSTOP, GPSTATE }

    public record CommandRequest(String host, int port, String user, String privateKey, CommandType type) {}
    public record CommandResponse(String status, String output) {}

    @PostMapping(path = "/exec", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<CommandResponse>> exec(@RequestBody CommandRequest request,
                                                      @org.springframework.beans.factory.annotation.Value("${plumchat.mgmt.enabled:false}") boolean mgmtEnabled,
                                                      @org.springframework.beans.factory.annotation.Value("${plumchat.mgmt.allowed-hosts:}") List<String> allowedHosts,
                                                      @org.springframework.beans.factory.annotation.Value("${plumchat.mgmt.allowed-users:}") List<String> allowedUsers) {
        if (!mgmtEnabled) {
            return Mono.just(ResponseEntity.status(403).body(new CommandResponse("DISABLED", "Management tools are disabled")));
        }
        if (!isAllowed(request, allowedHosts, allowedUsers)) {
            return Mono.just(ResponseEntity.status(403).body(new CommandResponse("FORBIDDEN", "Host or user not allowed")));
        }
        return Mono.fromCallable(() -> runAllowedCommand(request))
                .subscribeOn(Schedulers.boundedElastic())
                .map(resp -> ResponseEntity.ok(new CommandResponse("OK", resp)));
    }

    private boolean isAllowed(CommandRequest req, List<String> allowedHosts, List<String> allowedUsers) {
        boolean hostOk = allowedHosts == null || allowedHosts.isEmpty() || allowedHosts.contains(req.host());
        boolean userOk = allowedUsers == null || allowedUsers.isEmpty() || allowedUsers.contains(req.user());
        return hostOk && userOk;
    }

    private String runAllowedCommand(CommandRequest req) throws Exception {
        String cmd = switch (req.type()) {
            case GPSTART -> "gpstart -a";
            case GPSTOP -> "gpstop -a";
            case GPSTATE -> "gpstate -s";
        };
        return execSsh(req.host(), req.port(), req.user(), req.privateKey(), cmd);
    }

    private String execSsh(String host, int port, String user, String privateKey, String command) throws Exception {
        JSch jsch = new JSch();
        jsch.addIdentity("plumchat", privateKey.getBytes(StandardCharsets.UTF_8), null, null);
        Session session = jsch.getSession(user, host, port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(20000);
        try {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            channel.setOutputStream(baos);
            channel.connect(20000);
            while (!channel.isClosed()) {
                Thread.sleep(200);
            }
            return baos.toString(StandardCharsets.UTF_8);
        } finally {
            session.disconnect();
        }
    }
}


