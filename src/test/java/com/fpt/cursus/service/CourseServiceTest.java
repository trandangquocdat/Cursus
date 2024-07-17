package com.fpt.cursus.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.dto.request.CreateCourseDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.repository.AccountRepo;
import com.fpt.cursus.repository.CourseRepo;
import com.fpt.cursus.repository.FeedbackRepo;
import com.fpt.cursus.service.impl.CourseServiceImpl;
import com.fpt.cursus.util.AccountUtil;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @InjectMocks
    private CourseServiceImpl courseService;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private CourseRepo courseRepo;

    @Mock
    private AccountRepo accountRepo;

    @Mock
    private FeedbackRepo feedbackRepo;

    @Mock
    private AccountUtil accountUtil;

    private Account account;
    private CreateCourseDto createCourseDto;
    private Course course;


}

