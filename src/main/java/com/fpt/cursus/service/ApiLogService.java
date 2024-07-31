package com.fpt.cursus.service;

public interface ApiLogService {
    void saveApiLog(String requestUrl, String queryString);
}
