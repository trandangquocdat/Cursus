package com.fpt.cursus.repository;

import com.fpt.cursus.entity.IpLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.ZonedDateTime;

public interface IpLogRepo extends JpaRepository<IpLog, Long> {

    IpLog findByIpAddressAndApiEndpoint(String ipAddress, String apiEndpoint);

    @Query("SELECT COUNT(a) FROM IpLog a WHERE a.ipAddress = :ipAddress AND a.apiEndpoint = :apiEndpoint AND a.accessTime > :time")
    int countAccesses(String ipAddress, String apiEndpoint, ZonedDateTime time);

    void deleteByAccessTimeBefore(ZonedDateTime time);
}
