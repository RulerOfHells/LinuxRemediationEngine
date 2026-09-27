package com.linuxremediation.Engine.service;

import com.linuxremediation.Engine.domain.RemediationReport;
import com.linuxremediation.Engine.dto.RemediationContext;
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

    public RemediationReport executeRemediation(RemediationContext context) {
        log.info("Finding matching Strategy for alert [{}] on server [{}] with IP [{}]", context.getAlertPayloadDTO().getAlertName(), context.getServer().getHostname(), context.getServer().getIpAddress());

        RemediationStrategy remediationStrategy = strategies.stream()
                .filter(s -> s.supports(context.getAlertPayloadDTO()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Alert not supported"));

        log.info("Executing strategy [{}]", remediationStrategy.getClass().getSimpleName());
        return remediationStrategy.service(context);
    }
}
