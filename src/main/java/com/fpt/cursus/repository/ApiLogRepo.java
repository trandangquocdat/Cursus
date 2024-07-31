package com.fpt.cursus.repository;

import com.fpt.cursus.entity.ApiLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApiLogRepo extends JpaRepository<ApiLog, Long> {
    List<ApiLog> findByRequestUrl(String requestUrl);
}
