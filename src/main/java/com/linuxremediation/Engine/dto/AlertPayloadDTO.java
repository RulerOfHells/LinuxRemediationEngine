package com.linuxremediation.Engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertPayloadDTO {
    private String alertId;        // e.g., "ALT-2026-9081"
    private String alertName;      // e.g., "HighDiskUtilization"
    private String priority;       // e.g., "CRITICAL", "WARNING"
    private String server;     // e.g., IP address or hostname
    private String summary;        // Brief description of the issue
    private Map<String, String> metadata;
}
