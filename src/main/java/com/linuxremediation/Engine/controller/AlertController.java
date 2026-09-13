package com.linuxremediation.Engine.controller;

import com.linuxremediation.Engine.domain.Incident;
import com.linuxremediation.Engine.dto.AlertPayloadDTO;
import com.linuxremediation.Engine.dto.SnowIncidentPayloadDTO;
import com.linuxremediation.Engine.mapper.AlertMapper;
import com.linuxremediation.Engine.repository.IncidentRepository;
import com.linuxremediation.Engine.service.IncidentHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/alerts")
@Slf4j
@RequiredArgsConstructor
public class AlertController {

    private final AlertMapper alertMapper;
    private final IncidentHandlerService incidentHandlerService;
    private final IncidentRepository incidentRepository;

    @GetMapping
    public ResponseEntity<List<Incident>> getAllIncidents() {
        return ResponseEntity.ok(incidentRepository.findAll());
    }

    @ConditionalOnProperty(
            name = "remediationengine.incomingalert.snow-webhook.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    @PostMapping("/snow-webhook")
    public ResponseEntity<Map<String, Object>> snowWebhook(@RequestBody SnowIncidentPayloadDTO snowIncidentPayloadDTO) {
        log.info("Received Service Now Incident [{}] for server [{}]", snowIncidentPayloadDTO.getIncidentNumber(), snowIncidentPayloadDTO.getTargetServer());
        AlertPayloadDTO alertPayloadDTO = alertMapper.fromSnowIncidentPayload(snowIncidentPayloadDTO);
        String incidentId = incidentHandlerService.processIncomingAlert(alertPayloadDTO);
        return ResponseEntity.ok(Map.of(
                "status", "ACCEPTED",
                "source", "ServiceNow",
                "snowIncident", alertPayloadDTO.getAlertId(),
                "engineIncidentId", incidentId
        ));
    }

    @ConditionalOnProperty(name = "remediation.ingestion.direct-webhook.enabled", havingValue = "true")
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> webhook(@RequestBody AlertPayloadDTO alertPayloadDTO) {
        log.info("Received monitoring alert [{}] for server [{}]", alertPayloadDTO.getAlertId(), alertPayloadDTO.getServer());

        String incidentId = incidentHandlerService.processIncomingAlert(alertPayloadDTO);

        return ResponseEntity.ok(Map.of(
                "status", "ACCEPTED",
                "source", "Monitoring",
                "engineIncidentId", incidentId
        ));
    }

}
