package com.linuxremediation.Engine.dto;

import com.linuxremediation.Engine.domain.Incident;
import com.linuxremediation.Engine.domain.Server;
import com.linuxremediation.Engine.service.SSHExecutionService;
import lombok.Builder;
import lombok.Getter;

@Builder
public class RemediationContext {
    @Getter
    private final Server server;
    @Getter
    private final AlertPayloadDTO alertPayloadDTO;

    private final Incident incident;
    private final SSHExecutionService  sshExecutionService;

    public CommandResultDTO executeCommand(String command) {
        return sshExecutionService.executeCommand(this.incident, this.server, command);
    }

    public String getIncidentId() {
        return incident.getAlertId();
    }
}
