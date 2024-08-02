package com.fpt.cursus.service;

import com.fpt.cursus.entity.BlackListIp;
import com.fpt.cursus.entity.IpLog;
import com.fpt.cursus.repository.BlackListIpRepo;
import com.fpt.cursus.repository.IpLogRepo;
import com.fpt.cursus.service.impl.IpLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IpLogServiceTest {

    @InjectMocks
    private IpLogServiceImpl ipLogService;

    @Mock
    private IpLogRepo ipLogRepo;

    @Mock
    private BlackListIpRepo blackListIpRepo;

    private IpLog ipLog;

    @Test
    void testDeleteOldCounts() {
        ipLogService.deleteOldCounts();
        verify(ipLogRepo, times(1)).deleteByAccessTimeBefore(any(ZonedDateTime.class));
    }

    @BeforeEach
    void setUp() {
        ipLog = new IpLog();
        ipLog.setId(1);
        ipLog.setIpAddress("test");
        ipLog.setApiEndpoint("test");
        ipLog.setAccessTime(ZonedDateTime.now());
        ipLog.setCount(121);
    }

    @Test
    void testLogAccessNotNull() {
        //when
        when(ipLogRepo.findByIpAddressAndApiEndpoint(anyString(), anyString())).thenReturn(ipLog);
        when(ipLogRepo.save(any(IpLog.class))).thenReturn(ipLog);
        when(blackListIpRepo.findByIpAddress(anyString())).thenReturn(Optional.empty());
        //then
        ipLogService.logAccess("test", "test");

        verify(ipLogRepo, times(2)).findByIpAddressAndApiEndpoint(anyString(), anyString());
        verify(ipLogRepo, times(1)).save(ipLog);
        verify(blackListIpRepo, times(1)).save(any(BlackListIp.class));
    }

    @Test
    void testLogAccessIpLogNull() {
        //when
        when(ipLogRepo.findByIpAddressAndApiEndpoint(anyString(), anyString())).thenReturn(null);
        when(ipLogRepo.save(any(IpLog.class))).thenReturn(ipLog);
        //then
        ipLogService.logAccess("test", "test");

        verify(ipLogRepo, times(2)).findByIpAddressAndApiEndpoint(anyString(), anyString());
        verify(ipLogRepo, times(1)).save(any(IpLog.class));
        verify(blackListIpRepo, times(0)).save(any(BlackListIp.class));
    }

    @Test
    void testLogAccessLogCountLower() {
        //given
        ipLog.setCount(119);
        //when
        when(ipLogRepo.findByIpAddressAndApiEndpoint(anyString(), anyString())).thenReturn(ipLog);
        when(ipLogRepo.save(any(IpLog.class))).thenReturn(ipLog);
        //then
        ipLogService.logAccess("test", "test");

        verify(ipLogRepo, times(2)).findByIpAddressAndApiEndpoint(anyString(), anyString());
        verify(ipLogRepo, times(1)).save(any(IpLog.class));
        verify(blackListIpRepo, times(0)).save(any(BlackListIp.class));
    }

    @Test
    void testLogAccessBlackListIpNotNull() {
        //given
        BlackListIp blackListIp = new BlackListIp();
        blackListIp.setId(1L);
        blackListIp.setIpAddress("test");
        blackListIp.setBanTime(ZonedDateTime.now());
        blackListIp.setUsername("test");
        //when
        when(ipLogRepo.findByIpAddressAndApiEndpoint(anyString(), anyString())).thenReturn(ipLog);
        when(ipLogRepo.save(any(IpLog.class))).thenReturn(ipLog);
        when(blackListIpRepo.findByIpAddress(anyString())).thenReturn(Optional.of(blackListIp));
        //then
        ipLogService.logAccess("test", "test");

        verify(ipLogRepo, times(2)).findByIpAddressAndApiEndpoint(anyString(), anyString());
        verify(ipLogRepo, times(1)).save(ipLog);
        verify(blackListIpRepo, times(0)).save(any(BlackListIp.class));
    }
}
