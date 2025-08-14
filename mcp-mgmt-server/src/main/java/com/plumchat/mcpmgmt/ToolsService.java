package com.plumchat.mcpmgmt;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class ToolsService {

    private static final Logger log = LoggerFactory.getLogger(ToolsService.class);

    @Value("${plumchat.mgmt.allowed-hosts:}")
    private List<String> allowedHosts;

    @Value("${plumchat.mgmt.allowed-users:}")
    private List<String> allowedUsers;

    @Value("${plumchat.mgmt.allowed-key-dirs:}")
    private List<String> allowedKeyDirs;

    @Tool(description = "Run gpstart on the remote Greenplum master via SSH")
    public String gpstart(String host, int port, String user, String privateKey) throws Exception {
        return runSsh(host, port, user, privateKey, null, "gpstart -a");
    }

    @Tool(description = "Run gpstop on the remote Greenplum master via SSH")
    public String gpstop(String host, int port, String user, String privateKey) throws Exception {
        return runSsh(host, port, user, privateKey, null, "gpstop -a");
    }

    @Tool(description = "Run gpstate on the remote Greenplum master via SSH")
    public String gpstate(String host, int port, String user, String privateKey) throws Exception {
        return runSsh(host, port, user, privateKey, null, "gpstate -s");
    }

    @Tool(name = "gpstart_by_path", description = "Run gpstart using a private key loaded from an allowed directory on server")
    public String gpstartByPath(String host, int port, String user, String privateKeyPath) throws Exception {
        return runSsh(host, port, user, null, privateKeyPath, "gpstart -a");
    }

    @Tool(name = "gpstop_by_path", description = "Run gpstop using a private key loaded from an allowed directory on server")
    public String gpstopByPath(String host, int port, String user, String privateKeyPath) throws Exception {
        return runSsh(host, port, user, null, privateKeyPath, "gpstop -a");
    }

    @Tool(name = "gpstate_by_path", description = "Run gpstate using a private key loaded from an allowed directory on server")
    public String gpstateByPath(String host, int port, String user, String privateKeyPath) throws Exception {
        return runSsh(host, port, user, null, privateKeyPath, "gpstate -s");
    }

    private String runSsh(String host, int port, String user, String privateKey, String privateKeyPath, String command) throws Exception {
        if (!isAllowedHost(host) || !isAllowedUser(user)) {
            throw new SecurityException("Host or user not allowed");
        }
        byte[] keyBytes = resolveKeyBytes(privateKey, privateKeyPath);
        return execSsh(host, port, user, keyBytes, command);
    }

    private boolean isAllowedHost(String host) {
        return allowedHosts == null || allowedHosts.isEmpty() || allowedHosts.contains(host);
    }

    private boolean isAllowedUser(String user) {
        return allowedUsers == null || allowedUsers.isEmpty() || allowedUsers.contains(user);
    }

    private byte[] resolveKeyBytes(String privateKey, String privateKeyPath) throws IOException {
        if (privateKeyPath != null && !privateKeyPath.isBlank()) {
            Path path = Path.of(privateKeyPath).toAbsolutePath().normalize();
            if (!isUnderAllowedDir(path)) {
                throw new SecurityException("Private key path not under allowed directories");
            }
            return Files.readAllBytes(path);
        }
        if (privateKey != null && !privateKey.isBlank()) {
            return privateKey.getBytes(StandardCharsets.UTF_8);
        }
        throw new SecurityException("No private key provided");
    }

    private boolean isUnderAllowedDir(Path path) throws IOException {
        if (allowedKeyDirs == null || allowedKeyDirs.isEmpty()) {
            return false;
        }
        Path canonical = path.toRealPath();
        for (String dir : allowedKeyDirs) {
            Path allowed = Path.of(dir).toAbsolutePath().normalize();
            try {
                Path allowedReal = allowed.toRealPath();
                if (canonical.startsWith(allowedReal)) {
                    return true;
                }
            } catch (IOException ignore) {
                // skip non-existent allowed dirs
            }
        }
        return false;
    }

    private String execSsh(String host, int port, String user, byte[] privateKeyBytes, String command) throws Exception {
        JSch jsch = new JSch();
        jsch.addIdentity("plumchat", privateKeyBytes, null, null);
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
            log.info("Executed mgmt command on host={} user={} cmd={} status={}", host, user, command, channel.getExitStatus());
            return baos.toString(StandardCharsets.UTF_8);
        } finally {
            session.disconnect();
        }
    }
}


