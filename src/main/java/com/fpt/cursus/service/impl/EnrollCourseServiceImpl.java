package com.fpt.cursus.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.service.AccountService;
import com.fpt.cursus.service.EnrollCourseService;
import com.fpt.cursus.util.AccountUtil;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class EnrollCourseServiceImpl implements EnrollCourseService {
    private final AccountUtil accountUtil;
    private final AccountService accountService;

    public EnrollCourseServiceImpl(AccountUtil accountUtil, AccountService accountService) {
        this.accountUtil = accountUtil;
        this.accountService = accountService;
    }

    @Transactional
    public void enrollCourseAfterPay(List<Long> ids, String username) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Account account = accountService.getAccountByUsername(username);

        if (account.getEnrolledCourseJson() != null) {
            List<Long> enrolledCourse = mapper.readValue(account.getEnrolledCourseJson(), new TypeReference<>() {
            });
            Set<Long> enrolledCourseSet = new HashSet<>(enrolledCourse);
            enrolledCourseSet.addAll(ids);
            enrolledCourse = new ArrayList<>(enrolledCourseSet);
            account.setEnrolledCourse(enrolledCourse);
            account.setEnrolledCourseJson(mapper.writeValueAsString(enrolledCourse));
        } else {
            account.setEnrolledCourse(ids);
            account.setEnrolledCourseJson(mapper.writeValueAsString(ids));
        }
        accountService.saveAccount(account);
    }

}
