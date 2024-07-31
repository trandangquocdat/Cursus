package com.fpt.cursus.repository;

import com.fpt.cursus.entity.BlackListIp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlackListIpRepo extends JpaRepository<BlackListIp, Long> {
    Optional<BlackListIp> findByIpAddress(String ipAddress);
}