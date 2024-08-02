package com.fpt.cursus.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.service.AccountService;
import com.fpt.cursus.service.CourseService;
import com.fpt.cursus.service.EnrollCourseService;
import com.fpt.cursus.util.AccountUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class EnrollCourseServiceImpl implements EnrollCourseService {
    private final AccountService accountService;
    private final CourseService courseService;
    private final ObjectMapper objectMapper;
    private final AccountUtil accountUtil;

    public EnrollCourseServiceImpl(AccountService accountService,
                                   CourseService courseService,
                                   ObjectMapper objectMapper,
                                   AccountUtil accountUtil) {
        this.accountService = accountService;
        this.courseService = courseService;
        this.objectMapper = objectMapper;
        this.accountUtil = accountUtil;
    }

    @Override
    public void enrollCourseAfterPay(List<Long> ids, String username) throws JsonProcessingException {
        Account account = accountService.getAccountByUsername(username);
        Set<Long> purchasedCourseSet = new HashSet<>();

        if (account.getPurchasedCourseJson() != null) {
            List<Long> purchasedCourse = objectMapper.readValue(account.getPurchasedCourseJson(), new TypeReference<>() {
            });
            purchasedCourseSet.addAll(purchasedCourse);
        }

        purchasedCourseSet.addAll(ids);
        account.setPurchasedCourseJson(objectMapper.writeValueAsString(new ArrayList<>(purchasedCourseSet)));
        accountService.saveAccount(account);

    }

    @Override
    public void enrollCourse(Long id) {
        Account account = accountUtil.getCurrentAccount();
        List<Long> purchasedCourse;
        Course course = courseService.getCourseById(id);
        try {
            if (account.getPurchasedCourseJson() == null || account.getPurchasedCourseJson().isEmpty()) {
                throw new AppException(ErrorCode.COURSE_ENROLL_FAIL);
            }
            purchasedCourse = objectMapper.readValue(account.getPurchasedCourseJson(), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.COURSE_ENROLL_FAIL);
        }

        if (!purchasedCourse.contains(id)) {
            throw new AppException(ErrorCode.COURSE_ENROLL_FAIL);
        }

        List<Long> enrolledCourses = getEnrolledCoursesJson(account);
        if (enrolledCourses.contains(id)) {
            throw new AppException(ErrorCode.COURSE_ENROLL_EXISTS);
        }
        enrolledCourses.add(id);
        course.setEnroller(course.getEnroller() + 1);
        courseService.saveCourse(course);
        saveEnrolledCoursesJson(account, enrolledCourses);
    }

    private List<Long> getEnrolledCoursesJson(Account account) {
        if (account.getEnrolledCourseJson() == null || account.getEnrolledCourseJson().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(account.getEnrolledCourseJson(), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.COURSE_ENROLL_FAIL);
        }
    }

    private void saveEnrolledCoursesJson(Account account, List<Long> coursesId) {
        try {
            account.setEnrolledCourseJson(objectMapper.writeValueAsString(coursesId));
            accountService.saveAccount(account);
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.COURSE_ENROLL_FAIL);
        }
    }
}
