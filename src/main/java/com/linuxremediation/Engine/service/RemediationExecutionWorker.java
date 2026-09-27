package com.linuxremediation.Engine.service;

import com.linuxremediation.Engine.domain.Incident;
import com.linuxremediation.Engine.domain.RemediationReport;
import com.linuxremediation.Engine.domain.Server;
import com.linuxremediation.Engine.dto.AlertPayloadDTO;
import com.linuxremediation.Engine.dto.RemediationContext;
import com.linuxremediation.Engine.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class RemediationExecutionWorker {

    private final SSHExecutionService sshExecutionService;
    private final RuleEngineService ruleEngineService;
    private final IncidentRepository incidentRepository;
    private final ObjectMapper objectMapper;

    @Async
    public void executeRemediation(Incident incident, Server server, AlertPayloadDTO alertPayload) {
        log.info("Starting background remediation for incident [{}] on thread [{}]", incident.getAlertId(), Thread.currentThread().getName());

        RemediationContext context = RemediationContext.builder()
                .incident(incident)
                .server(server)
                .alertPayloadDTO(alertPayload)
                .sshExecutionService(sshExecutionService)
                .build();

        try {
            RemediationReport report = ruleEngineService.executeRemediation(context);

            if (report.getOutcome() == RemediationReport.OutcomeStatus.AUTOMATIC_RESOLVED) {
                incident.setStatus(Incident.IncidentStatus.AUTO_RESOLVED);
                incident.setResolvedAt(LocalDateTime.now());
            } else if (report.getOutcome() == RemediationReport.OutcomeStatus.ACTION_REQUIRED) {
                incident.setStatus(Incident.IncidentStatus.ESCALATED_TO_HUMAN);
            } else {
                incident.setStatus(Incident.IncidentStatus.FAILED);
            }

            incident.setRawReportJson(objectMapper.writeValueAsString(report));
        }
        catch (Exception e) {
            log.error("Async execution failed: {}", e.getMessage());
            incident.setStatus(Incident.IncidentStatus.FAILED);
        }
        finally {
            incident.setUpdatedAt(LocalDateTime.now());
            incidentRepository.save(incident);
        }
    }
}
