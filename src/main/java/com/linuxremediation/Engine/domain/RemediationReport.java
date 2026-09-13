package com.linuxremediation.Engine.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemediationReport {

    private String alertId;
    private String serverHostname;
    private String strategyName;
    private OutcomeStatus outcome;

    private String summary;

    @Builder.Default
    private List<ExecutionStep> executionTimeline = new ArrayList<>();

    private String detailedAnalysis;
    private EscalationDraft escalationDraft;

    @Builder.Default
    private List<ReportArtifact> artifacts = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionStep {
        private String phase;
        private String command;
        private int exitCode;
        private String stdout;
        private String stderr;
        private LocalDateTime timeStamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EscalationDraft {
        private String recipientGroup;
        private String subject;
        private String body;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportArtifact {
        private String title;
        private String mimeType;
        private String content;
    }

    public enum OutcomeStatus {
        AUTOMATIC_RESOLVED,
        ACTION_REQUIRED,
        FAILED
    }
}
