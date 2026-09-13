package com.linuxremediation.Engine.repository;

import com.linuxremediation.Engine.domain.Server;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServerRepository extends JpaRepository<Server, Integer> {
    Optional<Server> findByIpAddress(String ipAddress);
    Optional<Server> findByHostname(String hostname);
}
