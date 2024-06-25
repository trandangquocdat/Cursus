package com.fpt.cursus.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.repository.AccountRepo;
import com.fpt.cursus.util.AccountUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollCourseServiceTest {

    @Mock
    private AccountUtil accountUtil;

    @Mock
    private AccountRepo accountRepo;

    @InjectMocks
    private EnrollCourseService enrollCourseService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setUsername("username");
    }

    @Test
    void testEnrollCourseEnrolledCourseJsonNotNull() throws JsonProcessingException {
        account.setEnrolledCourseJson("[3,4]");
        List<Long> list = new ArrayList<>();
        list.add(1L);
        list.add(2L);

        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(accountRepo.save(any(Account.class))).thenReturn(account);

        enrollCourseService.enrollCourseAfterPay(list);

        assertEquals("[1,2,3,4]", account.getEnrolledCourseJson());
        assertEquals(4, account.getEnrolledCourse().size());
    }
}
