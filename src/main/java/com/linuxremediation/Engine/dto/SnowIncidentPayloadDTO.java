package com.linuxremediation.Engine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SnowIncidentPayloadDTO {

    @JsonProperty("sys_id")
    private String sysid;

    @JsonProperty("number")
    private String incidentNumber;

    @JsonProperty("short_description")
    private String shortDescription;

    @JsonProperty("cmdb_ci")
    private String targetServer;

    private String priority;

    @JsonProperty("assignment_group")
    private String assignmentGroup;
}
