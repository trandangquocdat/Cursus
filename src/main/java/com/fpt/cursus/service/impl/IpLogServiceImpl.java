package com.fpt.cursus.service.impl;

import com.fpt.cursus.entity.BlackListIp;
import com.fpt.cursus.entity.IpLog;
import com.fpt.cursus.repository.BlackListIpRepo;
import com.fpt.cursus.repository.IpLogRepo;
import com.fpt.cursus.service.IpLogService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;


@Service
public class IpLogServiceImpl implements IpLogService {

    private final IpLogRepo ipLogRepo;

    private final BlackListIpRepo blackListIpRepo;

    public IpLogServiceImpl(IpLogRepo ipLogRepo, BlackListIpRepo blackListIpRepo) {
        this.ipLogRepo = ipLogRepo;
        this.blackListIpRepo = blackListIpRepo;
    }

    @Transactional
    @Scheduled(fixedRate = 10000)
    public void deleteOldCounts() {
        ZonedDateTime timeDeleteRecord = ZonedDateTime.now().minusSeconds(10);
        ipLogRepo.deleteByAccessTimeBefore(timeDeleteRecord);
    }

    public void logAccess(String ipAddress, String apiEndpoint) {
        ZonedDateTime now = ZonedDateTime.now();
        IpLog existingLog = ipLogRepo.findByIpAddressAndApiEndpoint(ipAddress, apiEndpoint);
        if (existingLog != null) {
            existingLog.setCount(existingLog.getCount() + 1);
            ipLogRepo.save(existingLog);
        } else {
            IpLog newLog = new IpLog();
            newLog.setIpAddress(ipAddress);
            newLog.setApiEndpoint(apiEndpoint);
            newLog.setCount(1);
            newLog.setAccessTime(now);
            ipLogRepo.save(newLog);
        }

        checkAndBanIfExceedLimit(ipAddress, apiEndpoint, now);
    }

    private void checkAndBanIfExceedLimit(String ipAddress, String apiEndpoint, ZonedDateTime now) {
        IpLog log = ipLogRepo.findByIpAddressAndApiEndpoint(ipAddress, apiEndpoint);
        if (log != null && log.getCount() > 120 && blackListIpRepo.findByIpAddress(ipAddress).isEmpty()) {
            BlackListIp bannedIp = new BlackListIp();
            bannedIp.setIpAddress(ipAddress);
            bannedIp.setBanTime(now);
            blackListIpRepo.save(bannedIp);
        }
    }
}