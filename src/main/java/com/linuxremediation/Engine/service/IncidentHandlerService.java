package com.linuxremediation.Engine.service;

import com.linuxremediation.Engine.domain.AuditLog;
import com.linuxremediation.Engine.domain.Incident;
import com.linuxremediation.Engine.domain.RemediationReport;
import com.linuxremediation.Engine.domain.Server;
import com.linuxremediation.Engine.dto.AlertPayloadDTO;
import com.linuxremediation.Engine.dto.CommandResultDTO;
import com.linuxremediation.Engine.repository.AuditLogRepository;
import com.linuxremediation.Engine.repository.IncidentRepository;
import com.linuxremediation.Engine.repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncidentHandlerService {
    private final IncidentRepository incidentRepository;
    private final ServerRepository serverRepository;
    private final AuditLogRepository auditLogRepository;
    private final SSHExecutionService sshExecutionService;
    private final RuleEngineService ruleEngineService;

    @Transactional
    public String processIncomingAlert(AlertPayloadDTO alertPayload) {
        log.info("Processing alert [{}] for host [{}]", alertPayload.getAlertId(), alertPayload.getServer());
        return incidentRepository.findByAlertId(alertPayload.getAlertId())
                .map(existingIncident -> {
                    log.warn("Incident already exists for Alert ID: [{}]. Skipping duplicate execution.", alertPayload.getAlertId());
                    return existingIncident.getAlertId();
                })
                .orElseGet(() -> executeNewIncidentWorkflow(alertPayload));
    }

    private String executeNewIncidentWorkflow(AlertPayloadDTO alertPayload) {
        Server server = serverRepository.findByIpAddress(alertPayload.getServer())
                .or(() -> serverRepository.findByHostname(alertPayload.getServer()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Target Server not registered in database: " + alertPayload.getServer()));

        Incident incident = Incident.builder()
                .alertId(alertPayload.getAlertId())
                .alertName(alertPayload.getAlertName())
                .priority(alertPayload.getPriority())
                .status(Incident.IncidentStatus.IN_PROGRESS)
                .server(server)
                .summary(alertPayload.getSummary())
                .createdAt(LocalDateTime.now())
                .build();

        incident = incidentRepository.save(incident);

        RemediationReport report = ruleEngineService.executeRemediation(alertPayload, server, sshExecutionService);

        for (RemediationReport.ExecutionStep step : report.getExecutionTimeline()) {
            AuditLog auditLog = AuditLog.builder()
                    .incident(incident)
                    .commandExecuted("[" + step.getPhase() + "] " + step.getCommand())
                    .exitCode(step.getExitCode())
                    .stdout(step.getStdout())
                    .stderr(step.getStderr())
                    .executedAt(step.getTimeStamp())
                    .build();
            auditLogRepository.save(auditLog);
        }

        if (report.getOutcome() == RemediationReport.OutcomeStatus.AUTOMATIC_RESOLVED) {
            incident.setStatus(Incident.IncidentStatus.AUTO_RESOLVED);
        } else if (report.getOutcome() == RemediationReport.OutcomeStatus.ACTION_REQUIRED) {
            incident.setStatus(Incident.IncidentStatus.ESCALATED_TO_HUMAN);
        } else {
            incident.setStatus(Incident.IncidentStatus.FAILED);
        }

        incidentRepository.save(incident);
        return incident.getAlertId();
    }
}
