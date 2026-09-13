package com.linuxremediation.Engine.service;

import com.linuxremediation.Engine.domain.Server;
import com.linuxremediation.Engine.dto.CommandResultDTO;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class SSHExecutionService {

    private static final int COMMAND_TIMEOUT = 10;

    public CommandResultDTO executeCommand(Server server, String command) {
        try (SSHClient sshClient = new SSHClient()) {
            sshClient.addHostKeyVerifier(new PromiscuousVerifier());
            sshClient.connect(server.getIpAddress(), server.getSshPort());

            if (server.getAuthType() == Server.AuthType.KEY)
                sshClient.authPublickey(server.getSshUser(), server.getPrivateKeyPath());
            else if (server.getAuthType() == Server.AuthType.PASSWORD)
                sshClient.authPassword(server.getSshUser(), server.getPassword());
            else
                throw new IllegalArgumentException("Unsupported auth type" + server.getAuthType());

            try (Session session = sshClient.startSession()) {
                Session.Command cmd = session.exec(command);

                var stdOutStream = new ByteArrayOutputStream();
                var stdErrStream = new ByteArrayOutputStream();
                cmd.getInputStream().transferTo(stdOutStream);
                cmd.getErrorStream().transferTo(stdErrStream);

                cmd.join(COMMAND_TIMEOUT, TimeUnit.SECONDS);

                int exitCode = cmd.getExitStatus() != null ? cmd.getExitStatus() : -1;

                return CommandResultDTO.builder()
                        .command(command)
                        .exitCode(exitCode)
                        .stdout(stdOutStream.toString())
                        .stderr(stdErrStream.toString())
                        .success(exitCode == 0)
                        .build();
            }
        }
        catch (IOException e) {
            return CommandResultDTO.builder()
                        .command(command)
                        .exitCode(-1)
                        .stdout("")
                        .stderr("Something went wrong: " + e.getMessage())
                        .success(false)
                        .build();
        }
    }
}
