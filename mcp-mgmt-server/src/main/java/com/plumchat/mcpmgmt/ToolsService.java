package com.baskettecase.plumchat.mcpmgmt;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class ToolsService {

    @Tool(description = "Run gpstart on the remote Greenplum master via SSH")
    public String gpstart(String host, int port, String user, String privateKey) throws Exception {
        return execSsh(host, port, user, privateKey, "gpstart -a");
    }

    @Tool(description = "Run gpstop on the remote Greenplum master via SSH")
    public String gpstop(String host, int port, String user, String privateKey) throws Exception {
        return execSsh(host, port, user, privateKey, "gpstop -a");
    }

    @Tool(description = "Run gpstate on the remote Greenplum master via SSH")
    public String gpstate(String host, int port, String user, String privateKey) throws Exception {
        return execSsh(host, port, user, privateKey, "gpstate -s");
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


