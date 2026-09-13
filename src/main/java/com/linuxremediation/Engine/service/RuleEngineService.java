package com.linuxremediation.Engine.service;

import com.linuxremediation.Engine.domain.RemediationReport;
import com.linuxremediation.Engine.domain.Server;
import com.linuxremediation.Engine.dto.AlertPayloadDTO;
import com.linuxremediation.Engine.strategy.RemediationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class RuleEngineService {

    private final List<RemediationStrategy> strategies;

    public RuleEngineService(List<RemediationStrategy> strategies) {
        this.strategies = strategies.stream()
                .sorted(Comparator.comparingInt(RemediationStrategy::order))
                .toList();
    }

    public RemediationReport executeRemediation(AlertPayloadDTO alert, Server server, SSHExecutionService sshService) {
        log.info("Finding matching Strategy for alert [{}] on server [{}] with IP [{}]", alert.getAlertName(), server.getHostname(), server.getIpAddress());

        RemediationStrategy remediationStrategy = strategies.stream()
                .filter(s -> s.supports(alert))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Alert not supported"));

        log.info("Executing strategy [{}]", remediationStrategy.getClass().getSimpleName());
        return remediationStrategy.service(alert, server, sshService);
    }
}
