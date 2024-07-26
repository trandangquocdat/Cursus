package com.fpt.cursus.service;

public interface ApiLogService {
    void logAccess(String ipAddress, String apiEndpoint);
}
