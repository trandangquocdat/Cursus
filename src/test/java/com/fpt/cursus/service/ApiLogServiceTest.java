package com.fpt.cursus.service;

import com.fpt.cursus.entity.ApiLog;
import com.fpt.cursus.entity.BackListIP;
import com.fpt.cursus.repository.ApiLogRepo;
import com.fpt.cursus.repository.BackListIPRepo;
import com.fpt.cursus.service.impl.ApiLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiLogServiceTest {

    @Mock
    private ApiLogRepo apiLogRepo;

    @Mock
    private BackListIPRepo backListIPRepo;

    @InjectMocks
    private ApiLogServiceImpl apiLogServiceImpl;

    private ApiLog apiLog;

    @BeforeEach
    void setUp() {
        apiLog = new ApiLog();
        apiLog.setIpAddress("127.0.0.1");
        apiLog.setApiEndpoint("/test");
        apiLog.setCount(1);
        apiLog.setAccessTime(ZonedDateTime.now());
    }

    @Test
    void deleteOldCounts() {
        apiLogServiceImpl.deleteOldCounts();

        verify(apiLogRepo, times(1)).deleteByAccessTimeBefore(any(ZonedDateTime.class));
    }

    @Test
    void logAccess_existingLog() {
        when(apiLogRepo.findByIpAddressAndApiEndpoint(anyString(), anyString())).thenReturn(apiLog);

        apiLogServiceImpl.logAccess("127.0.0.1", "/test");

        assertEquals(2, apiLog.getCount());
        verify(apiLogRepo, times(1)).save(apiLog);
    }

    @Test
    void logAccess_newLog() {
        when(apiLogRepo.findByIpAddressAndApiEndpoint(anyString(), anyString())).thenReturn(null);

        apiLogServiceImpl.logAccess("127.0.0.1", "/test");

        verify(apiLogRepo, times(1)).save(any(ApiLog.class));
    }

    @Test
    void checkAndBanIfExceedLimit_ban() {
        apiLog.setCount(101);
        when(apiLogRepo.findByIpAddressAndApiEndpoint(anyString(), anyString())).thenReturn(apiLog);
        when(backListIPRepo.findByIpAddress(anyString())).thenReturn(Optional.empty());

        apiLogServiceImpl.logAccess("127.0.0.1", "/test");

        verify(backListIPRepo, times(1)).save(any(BackListIP.class));
    }

    @Test
    void checkAndBanIfExceedLimit_doNotBan() {
        apiLog.setCount(50);
        when(apiLogRepo.findByIpAddressAndApiEndpoint(anyString(), anyString())).thenReturn(apiLog);

        apiLogServiceImpl.logAccess("127.0.0.1", "/test");

        verify(backListIPRepo, times(0)).save(any(BackListIP.class));
    }

    @Test
    void checkAndBanIfExceedLimit_alreadyBanned() {
        apiLog.setCount(101);
        when(apiLogRepo.findByIpAddressAndApiEndpoint(anyString(), anyString())).thenReturn(apiLog);
        when(backListIPRepo.findByIpAddress(anyString())).thenReturn(Optional.of(new BackListIP()));

        apiLogServiceImpl.logAccess("127.0.0.1", "/test");

        verify(backListIPRepo, times(0)).save(any(BackListIP.class));
    }
}