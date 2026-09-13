package com.linuxremediation.Engine.mapper;

import com.linuxremediation.Engine.dto.AlertPayloadDTO;
import com.linuxremediation.Engine.dto.SnowIncidentPayloadDTO;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AlertMapper {

    public AlertPayloadDTO fromSnowIncidentPayload(SnowIncidentPayloadDTO snowIncidentPayloadDTO) {
        return AlertPayloadDTO.builder()
                .alertId(snowIncidentPayloadDTO.getIncidentNumber())
                .alertName(extractAlertName(snowIncidentPayloadDTO.getShortDescription().toLowerCase()))
                .priority(mapSnowPriority(snowIncidentPayloadDTO.getPriority()))
                .server(snowIncidentPayloadDTO.getTargetServer())
                .summary(snowIncidentPayloadDTO.getShortDescription())
                .metadata(Map.of("source", "ServiceNow", "sysId", snowIncidentPayloadDTO.getSysid(), "assignmentGroup", snowIncidentPayloadDTO.getAssignmentGroup()))
                .build();
    }

    private String extractAlertName(String shortDescription) {
        if(shortDescription.contains("fs")) return "HighDiskUtilization";
        if(shortDescription.contains("cpu utilization") || shortDescription.contains("cpu load")) return "HighCPUUtilization";
        if(shortDescription.contains("memory utilization")) return "HighMemoryUtilization";
        if(shortDescription.contains("process") || shortDescription.contains("service")) return "ProcessNotRunning";
        if(shortDescription.contains("cron")) return "Crontab";
        return "unknown";
    }

    private String mapSnowPriority(String priority) {
        return switch(priority) {
            case "1" -> "CRITICAL";
            case "2" -> "HIGH";
            case "3" -> "MEDIUM";
            default -> "STANDARD";
        };
    }

}
