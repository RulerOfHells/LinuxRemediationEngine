package com.linuxremediation.Engine.service;

import com.linuxremediation.Engine.domain.Incident;
import com.linuxremediation.Engine.domain.Server;
import com.linuxremediation.Engine.dto.AlertPayloadDTO;
import com.linuxremediation.Engine.repository.IncidentRepository;
import com.linuxremediation.Engine.repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncidentHandlerService {
    private final IncidentRepository incidentRepository;
    private final ServerRepository serverRepository;
    private final RemediationExecutionWorker remediationExecutionWorker;

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

        remediationExecutionWorker.executeRemediation(incident, server, alertPayload);

        return incident.getAlertId();
    }
}
