package com.fpt.cursus.repository;

import com.fpt.cursus.entity.ApiLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.ZonedDateTime;

public interface ApiLogRepo extends JpaRepository<ApiLog, Long> {

    ApiLog findByIpAddressAndApiEndpoint(String ipAddress, String apiEndpoint);

    @Query("SELECT COUNT(a) FROM ApiLog a WHERE a.ipAddress = :ipAddress AND a.apiEndpoint = :apiEndpoint AND a.accessTime > :time")
    int countAccesses(String ipAddress, String apiEndpoint, ZonedDateTime time);

    void deleteByAccessTimeBefore (ZonedDateTime time);
}
