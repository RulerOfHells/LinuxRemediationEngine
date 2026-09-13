package com.linuxremediation.Engine.repository;

import com.linuxremediation.Engine.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {
    List<AuditLog> findByIncidentId(Long incidentId);
}
