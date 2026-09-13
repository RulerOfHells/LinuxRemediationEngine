package com.linuxremediation.Engine.strategy;

import com.linuxremediation.Engine.domain.RemediationReport;
import com.linuxremediation.Engine.domain.Server;
import com.linuxremediation.Engine.dto.AlertPayloadDTO;
import com.linuxremediation.Engine.service.SSHExecutionService;

public interface RemediationStrategy {
    boolean supports(AlertPayloadDTO alert);
    RemediationReport service(AlertPayloadDTO alert, Server server, SSHExecutionService sshService);

    default int order() {
        return 100;
    }
}
