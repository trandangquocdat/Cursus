package com.fpt.cursus.service.impl;

import com.fpt.cursus.entity.ApiLog;
import com.fpt.cursus.entity.BackListIP;
import com.fpt.cursus.repository.ApiLogRepo;
import com.fpt.cursus.repository.BackListIPRepo;
import com.fpt.cursus.service.ApiLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;


@Service
public class ApiLogServiceImpl implements ApiLogService {

    private final ApiLogRepo apiLogRepo;

    private final BackListIPRepo backListIPRepo;

    public ApiLogServiceImpl(ApiLogRepo apiLogRepo, BackListIPRepo backListIPRepo) {
        this.apiLogRepo = apiLogRepo;
        this.backListIPRepo = backListIPRepo;
    }

    @Transactional
    @Scheduled(fixedRate = 10000)
    public void deleteOldCounts() {
        ZonedDateTime timeDeleteRecord = ZonedDateTime.now().minusSeconds(10);
        apiLogRepo.deleteByAccessTimeBefore(timeDeleteRecord);
    }

    public void logAccess(String ipAddress, String apiEndpoint) {
        ZonedDateTime now = ZonedDateTime.now();
        ApiLog existingLog = apiLogRepo.findByIpAddressAndApiEndpoint(ipAddress, apiEndpoint);
        if (existingLog != null) {
            existingLog.setCount(existingLog.getCount() + 1);
            apiLogRepo.save(existingLog);
        } else {
            ApiLog newLog = new ApiLog();
            newLog.setIpAddress(ipAddress);
            newLog.setApiEndpoint(apiEndpoint);
            newLog.setCount(1);
            newLog.setAccessTime(now);
            apiLogRepo.save(newLog);
        }

        checkAndBanIfExceedLimit(ipAddress, apiEndpoint, now);
    }
    private void checkAndBanIfExceedLimit(String ipAddress, String apiEndpoint, ZonedDateTime now) {
        ApiLog log = apiLogRepo.findByIpAddressAndApiEndpoint(ipAddress, apiEndpoint);
        if (log != null && log.getCount() > 50) {
            if (!backListIPRepo.findByIpAddress(ipAddress).isPresent()) {
                BackListIP bannedIp = new BackListIP();
                bannedIp.setIpAddress(ipAddress);
                bannedIp.setBanTime(now);
                backListIPRepo.save(bannedIp);
            }
        }
    }


}

