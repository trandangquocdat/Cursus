package com.fpt.cursus.service;

public interface IpLogService {
    void logAccess(String ipAddress, String apiEndpoint);
}
