package com.fpt.cursus.repository;

import com.fpt.cursus.entity.BlackListIP;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlackListIPRepo extends JpaRepository<BlackListIP, Long> {
    Optional<BlackListIP> findByIpAddress(String ipAddress);
}