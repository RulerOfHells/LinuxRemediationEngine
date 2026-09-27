package com.linuxremediation.Engine.service;

import com.linuxremediation.Engine.domain.AuditLog;
import com.linuxremediation.Engine.domain.Incident;
import com.linuxremediation.Engine.dto.CommandResultDTO;
import com.linuxremediation.Engine.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLoggingService {
    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logExecution(Incident incident, CommandResultDTO commandResultDTO) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .incident(incident)
                    .commandExecuted(commandResultDTO.getCommand())
                    .exitCode(commandResultDTO.getExitCode())
                    .stdout(commandResultDTO.getStdout())
                    .stderr(commandResultDTO.getStderr())
                    .executedAt(LocalDateTime.now())
                    .build();
            auditLogRepository.save(auditLog);
        }
        catch (Exception e) {
            log.error("Failed to write audit log entry for command [{}] on incident [{}]",
                    commandResultDTO.getCommand(), incident.getAlertId(), e);
        }
    }
}
