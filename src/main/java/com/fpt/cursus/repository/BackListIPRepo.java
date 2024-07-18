package com.fpt.cursus.repository;

import com.fpt.cursus.entity.BackListIP;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BackListIPRepo extends JpaRepository<BackListIP, Long> {
    Optional<BackListIP> findByIpAddress(String ipAddress);
}