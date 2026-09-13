package com.linuxremediation.Engine.repository;

import com.linuxremediation.Engine.domain.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Integer> {
    Optional<Incident> findByAlertId(String alertId);
}
