package com.fpt.cursus.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.repository.AccountRepo;
import com.fpt.cursus.service.EnrollCourseService;
import com.fpt.cursus.util.AccountUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class EnrollCourseServiceImpl implements EnrollCourseService {
    @Autowired
    private AccountUtil accountUtil;
    @Autowired
    private AccountRepo accountRepo;

    @Transactional
    public void enrollCourseAfterPay(List<Long> ids) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Account account = accountUtil.getCurrentAccount();

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
        accountRepo.save(account);
    }

}
