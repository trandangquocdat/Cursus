package com.fpt.cursus.service;

import com.fpt.cursus.entity.ApiLog;
import com.fpt.cursus.repository.ApiLogRepo;
import com.fpt.cursus.service.impl.ApiLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiLogServiceTest {

    @Mock
    private ApiLogRepo apiLogRepo;

    @InjectMocks
    private ApiLogServiceImpl apiLogServiceImpl;

    private ApiLog apiLog;

    @BeforeEach
    void setUp() {
        apiLog = new ApiLog();
        apiLog.setId(1);
        apiLog.setRequestUrl("http://localhost:8080/api/v1/courses");
        apiLog.setQueryString("courseId=1");
        apiLog.setAccessTime(new Date());
        apiLog.setCount(1);
    }

    @Test
    void testSaveApiLogFoundIdParamTrueAndContainIdValueAndHitEndIndex() {
        //given
        String requestUrl = "http://localhost:8080/api/v1/courses";
        String queryString = "courseId=1";
        ApiLog apiLog2 = new ApiLog();
        apiLog2.setId(2);
        apiLog2.setRequestUrl("http://localhost:8080/api/v1/courses");
        apiLog2.setQueryString("courseId=2");
        apiLog2.setAccessTime(new Date());
        apiLog2.setCount(1);
        List<ApiLog> apiLogs = List.of(apiLog2, apiLog);
        //when
        when(apiLogRepo.findByRequestUrl(anyString())).thenReturn(apiLogs);
        //then
        apiLogServiceImpl.saveApiLog(requestUrl, queryString);
        verify(apiLogRepo, times(1)).save(apiLog);
    }

    @Test
    void testSaveApiLogFoundQueryStringNull() {
        //given
        String requestUrl = "http://localhost:8080/api/v1/courses";
        String queryString = null;
        //when
        //then
        apiLogServiceImpl.saveApiLog(requestUrl, queryString);
        verify(apiLogRepo, times(0)).save(apiLog);
    }

    @Test
    void testSaveApiLogQueryStringNotContainIdParam() {
        //given
        String requestUrl = "http://localhost:8080/api/v1/courses";
        String queryString = "1";
        //when
        //then
        apiLogServiceImpl.saveApiLog(requestUrl, queryString);
        verify(apiLogRepo, times(0)).save(apiLog);
    }

    @Test
    void testSaveApiLogNotContainIdValueAndNotHitEndIndex() {
        //given
        String requestUrl = "http://localhost:8080/api/v1/courses";
        String queryString = "courseId=2&courseId=3";
        List<ApiLog> apiLogs = List.of(apiLog);
        //when
        when(apiLogRepo.findByRequestUrl(anyString())).thenReturn(apiLogs);
        //then
        apiLogServiceImpl.saveApiLog(requestUrl, queryString);
        verify(apiLogRepo, times(1)).save(any(ApiLog.class));
    }

}