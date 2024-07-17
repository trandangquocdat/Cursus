//package com.fpt.cursus.service;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fpt.cursus.dto.object.StudiedCourse;
//import com.fpt.cursus.dto.request.CreateCourseDto;
//import com.fpt.cursus.entity.Account;
//import com.fpt.cursus.entity.Course;
//import com.fpt.cursus.entity.Feedback;
//import com.fpt.cursus.enums.CourseStatus;
//import com.fpt.cursus.enums.Category;
//import com.fpt.cursus.exception.exceptions.AppException;
//import com.fpt.cursus.exception.exceptions.ErrorCode;
//import com.fpt.cursus.repository.AccountRepo;
//import com.fpt.cursus.repository.CourseRepo;
//import com.fpt.cursus.repository.FeedbackRepo;
//import com.fpt.cursus.service.impl.CourseServiceImpl;
//import com.fpt.cursus.util.AccountUtil;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Sort;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class CourseServiceTest {
//
//    @InjectMocks
//    private CourseServiceImpl courseService;
//
//    @Mock
//    private ObjectMapper mapper;
//
//    @Mock
//    private CourseRepo courseRepo;
//
//    @Mock
//    private AccountRepo accountRepo;
//
//    @Mock
//    private FeedbackRepo feedbackRepo;
//
//    @Mock
//    private AccountUtil accountUtil;
//
//    private Account account;
//    private CreateCourseDto createCourseDto;
//    private Course course;
//
//
//
//}
//
