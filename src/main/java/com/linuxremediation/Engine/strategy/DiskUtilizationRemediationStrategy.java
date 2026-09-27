package com.linuxremediation.Engine.strategy;

import com.linuxremediation.Engine.domain.RemediationReport;
import com.linuxremediation.Engine.domain.Server;
import com.linuxremediation.Engine.dto.AlertPayloadDTO;
import com.linuxremediation.Engine.dto.CommandResultDTO;
import com.linuxremediation.Engine.dto.RemediationContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static com.linuxremediation.Engine.domain.RemediationReport.OutcomeStatus.AUTOMATIC_RESOLVED;
import static com.linuxremediation.Engine.domain.RemediationReport.OutcomeStatus.FAILED;

@Component
public class DiskUtilizationRemediationStrategy implements RemediationStrategy {

    @Override
    public boolean supports(AlertPayloadDTO alert) {
        if (alert == null || alert.getAlertName() == null) return false;
        String name = alert.getAlertName().toLowerCase();
        return name.contains("disk") || name.contains("filesystem") || name.contains("space");
    }

    @Override
    public RemediationReport service(RemediationContext context) {
        AlertPayloadDTO alert = context.getAlertPayloadDTO();
        Server server = context.getServer();
        String mountPoint = alert.getMetadata() != null ? alert.getMetadata().getOrDefault("mountPoint", "/var/log") : "/var/log";

        RemediationReport report = RemediationReport.builder()
                .alertId(alert.getAlertId())
                .serverHostname(server.getHostname())
                .strategyName("DiskUtilizationRemediationStrategy")
                .executionTimeline(new ArrayList<>())
                .artifacts(new ArrayList<>())
                .build();

        // 1. Initial Diagnostic: Inspect Filesystem Usage
        CommandResultDTO dfResult = context.executeCommand("df -Th " + mountPoint);
        addTimelineStep(report, "DIAGNOSTIC_DF", dfResult);

        // 2. Identify Top Space Consumers
        CommandResultDTO duResult = context.executeCommand("du -xhd1 " + mountPoint + "/* 2>/dev/null | sort -hr | head -n 10");
        addTimelineStep(report, "DIAGNOSTIC_DU", duResult);

        // Build CSV Artifact of top usage for engineers
        report.getArtifacts().add(RemediationReport.ReportArtifact.builder()
                .title("mount_usage_breakdown.csv")
                .mimeType("text/csv")
                .content("Directory,Size\n" + formatDuToCsv(duResult.getStdout()))
                .build());

        // 3. Branching Logic Based on Scope Boundary
        if (mountPoint.startsWith("/app") || mountPoint.startsWith("/u01") || mountPoint.startsWith("/data")) {
            // OUT OF LINUX OS SCOPE -> Generate Escalation Draft for Application Team
            report.setOutcome(RemediationReport.OutcomeStatus.ACTION_REQUIRED);
            report.setSummary("Disk threshold exceeded on Application Mount [" + mountPoint + "]. Out of OS automation scope.");

            report.setDetailedAnalysis("""
                    ### Scope Assessment: Application Directory
                    The mount point `%s` belongs to application/data space. Automated file deletion was **bypassed** to prevent data loss or service disruption.
                    
                    **Current Disk Usage:**
                    ```
                    %s
                    ```
                    """.formatted(mountPoint, dfResult.getStdout()));

            report.setEscalationDraft(RemediationReport.EscalationDraft.builder()
                    .recipientGroup("App-Support-Team@company.com")
                    .subject("URGENT: Application Disk Space Cleanup Required on " + server.getHostname() + " (" + mountPoint + ")")
                    .body("""
                            Hi Team,

                            An automated alert triggered for high disk utilization on host %s (%s).

                            Current Status:
                            %s

                            Top space consumers under %s:
                            %s

                            Please inspect application log rotation or purge old temp files.

                            Regards,
                            Linux Infrastructure Automation
                            """.formatted(server.getHostname(), server.getIpAddress(), dfResult.getStdout(), mountPoint, duResult.getStdout()))
                    .build());

        } else {
            // IN LINUX OS SCOPE (/var/log, /tmp, /var/cache) -> Attempt Automated Remediation
            CommandResultDTO basicCleanupResult = context.executeCommand(
                    "journalctl --vacuum-size=200M && dnf clean all"
            );
            addTimelineStep(report, "REMEDIATION_BASIC_CLEANUP", basicCleanupResult);

            CommandResultDTO logrotateResult = context.executeCommand(
                    "sudo logrotate -f /etc/logrotate.conf 2>/dev/null");
            addTimelineStep(report, "REMEDIATION_LOGROTATE", logrotateResult);

            // Re-verify space after remediation
            CommandResultDTO postVerifyResult = context.executeCommand("df -Th " + mountPoint);
            addTimelineStep(report, "POST_VERIFICATION_DF", postVerifyResult);

            Optional<Integer> use = getAvailableSpace(postVerifyResult.getStdout());

            report.setOutcome((use.isPresent() && use.get() > 85)? FAILED : AUTOMATIC_RESOLVED);
            report.setSummary("Automated log cleanup executed successfully on OS mount [" + mountPoint + "].");
            report.setDetailedAnalysis("""
                    ### Automated Action Taken:
                    - Forced systemd journal vacuuming to 200M.
                    - Triggered system logrotate configuration.
                    
                    **Filesystem State After Cleanup:**
                    ```
                    %s
                    ```
                    """.formatted(postVerifyResult.getStdout()));
        }

        return report;
    }

    private void addTimelineStep(RemediationReport report, String phase, CommandResultDTO result) {
        report.getExecutionTimeline().add(RemediationReport.ExecutionStep.builder()
                .phase(phase)
                .command(result.getCommand())
                .exitCode(result.getExitCode())
                .stdout(result.getStdout())
                .stderr(result.getStderr())
                .timeStamp(LocalDateTime.now())
                .build());
    }

    private String formatDuToCsv(String duOutput) {
        if (duOutput == null || duOutput.isBlank()) return "";
        StringBuilder csv = new StringBuilder();
        for (String line : duOutput.split("\n")) {
            String[] parts = line.trim().split("\\s+", 2);
            if (parts.length == 2) {
                csv.append(parts[1]).append(",").append(parts[0]).append("\n");
            }
        }
        return csv.toString();
    }

    private Optional<Integer> getAvailableSpace(String result) {
        String[] lines = result.split("\n");
        for (String line : lines) {
            if(line.contains("/"))
                return Optional.of(Integer.parseInt(line.split("\\s+")[5].replace("%", "")));
        }
        return Optional.empty();
    }
}