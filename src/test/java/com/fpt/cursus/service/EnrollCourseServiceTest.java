package com.fpt.cursus.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.service.impl.EnrollCourseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EnrollCourseServiceTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private EnrollCourseServiceImpl enrollCourseService;

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mapper = new ObjectMapper();
    }

    @Test
    void testEnrollCourseAfterPay() throws JsonProcessingException {
        List<Long> ids = List.of(1L, 2L);
        String username = "testuser";
        Account account = new Account();
        account.setUsername(username);

        when(accountService.getAccountByUsername(username)).thenReturn(account);
        doNothing().when(accountService).saveAccount(any(Account.class));

        enrollCourseService.enrollCourseAfterPay(ids, username);

        verify(accountService, times(1)).getAccountByUsername(username);
        verify(accountService, times(1)).saveAccount(any(Account.class));
    }

    @Test
    void testEnrollCourseAfterPay_WithExistingEnrolledCoursesJson() throws JsonProcessingException {
        List<Long> ids = List.of(3L, 4L);
        String username = "testuser";

        List<Long> existingEnrolledCourses = List.of(1L, 2L);
        String existingEnrolledCoursesJson = mapper.writeValueAsString(existingEnrolledCourses);
        Account account = new Account();
        account.setUsername(username);
        account.setEnrolledCourseJson(existingEnrolledCoursesJson);

        when(accountService.getAccountByUsername(username)).thenReturn(account);
        doNothing().when(accountService).saveAccount(any(Account.class));

        enrollCourseService.enrollCourseAfterPay(ids, username);

        verify(accountService, times(1)).getAccountByUsername(username);
        verify(accountService, times(1)).saveAccount(any(Account.class));

        List<Long> expectedEnrolledCourses = new ArrayList<>(existingEnrolledCourses);
        expectedEnrolledCourses.addAll(ids);
        assertEquals(expectedEnrolledCourses, account.getEnrolledCourse());
        assertEquals(mapper.writeValueAsString(expectedEnrolledCourses), account.getEnrolledCourseJson());
    }

}