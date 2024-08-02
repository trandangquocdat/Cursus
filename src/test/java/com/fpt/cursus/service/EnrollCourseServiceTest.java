package com.fpt.cursus.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.service.impl.EnrollCourseServiceImpl;
import com.fpt.cursus.util.AccountUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.cglib.core.ReflectUtils.getClasses;

@ExtendWith(MockitoExtension.class)
class EnrollCourseServiceTest {

    @Mock
    private AccountService accountService;

    @Mock
    private CourseService courseService;

    @Mock
    private AccountUtil accountUtil;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private EnrollCourseServiceImpl enrollCourseService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setUsername("test");
        account.setPurchasedCourseJson("[]");
    }

    @Test
    void testEnrollCourseAfterPay() throws JsonProcessingException {
        //when
        when(accountService.getAccountByUsername(anyString())).thenReturn(account);
        when(mapper.readValue(anyString(), any(TypeReference.class))).thenReturn(new ArrayList<>());
        //then
        enrollCourseService.enrollCourseAfterPay(List.of(2L), "test");

        verify(accountService, times(1)).getAccountByUsername(anyString());
        verify(accountService, times(1)).saveAccount(any(Account.class));
    }

    @Test
    void testEnrollCourseAfterPayNull() throws JsonProcessingException {
        //given
        account.setPurchasedCourseJson(null);
        //when
        when(accountService.getAccountByUsername(anyString())).thenReturn(account);
        //then
        enrollCourseService.enrollCourseAfterPay(List.of(2L), "test");
        verify(accountService, times(1)).getAccountByUsername(anyString());
        verify(accountService, times(1)).saveAccount(any(Account.class));
        verify(mapper, times(1)).writeValueAsString(any());
        verify(mapper, never()).readValue(anyString(), any(TypeReference.class));
    }

    @Test
    void testEnrollCourse() throws JsonProcessingException {
        //given
        Course course = new Course();
        course.setId(1L);
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(mapper.readValue(anyString(), any(TypeReference.class))).thenReturn(List.of(1L));
        when(courseService.getCourseById(ArgumentMatchers.anyLong())).thenReturn(course);
        //then
        enrollCourseService.enrollCourse(1L);
        verify(accountUtil, times(1)).getCurrentAccount();
        verify(courseService, times(1)).getCourseById(ArgumentMatchers.anyLong());
        verify(courseService, times(1)).saveCourse(ArgumentMatchers.any(Course.class));
        verify(accountService, times(1)).saveAccount(ArgumentMatchers.any(Account.class));
    }

    @Test
    void testEnrollCourseFailPurchased() throws JsonProcessingException {
        //given
        Course course = new Course();
        course.setId(1L);
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(courseService.getCourseById(ArgumentMatchers.anyLong())).thenReturn(course);
        when(mapper.readValue(anyString(), any(TypeReference.class))).thenThrow(JsonProcessingException.class);
        //then
        assertThrows(AppException.class,
                () -> enrollCourseService.enrollCourse(1L),
                ErrorCode.COURSE_ENROLL_FAIL.getMessage());
    }

    @Test
    void testEnrollCoursePurchasedNotContain() throws JsonProcessingException {
        //given
        Course course = new Course();
        course.setId(1L);
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(courseService.getCourseById(ArgumentMatchers.anyLong())).thenReturn(course);
        when(mapper.readValue(anyString(), any(TypeReference.class))).thenReturn(List.of(2L));
        //then
        assertThrows(AppException.class,
                () -> enrollCourseService.enrollCourse(1L),
                ErrorCode.COURSE_ENROLL_FAIL.getMessage());
    }

    @Test
    void testEnrollCourseEnrolled() throws JsonProcessingException {
        //given
        Course course = new Course();
        course.setId(1L);
        account.setEnrolledCourseJson("[1]");
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(courseService.getCourseById(ArgumentMatchers.anyLong())).thenReturn(course);
        when(mapper.readValue(anyString(), any(TypeReference.class))).thenReturn(List.of(1L));
        //then
        assertThrows(AppException.class,
                () -> enrollCourseService.enrollCourse(1L),
                ErrorCode.COURSE_ENROLL_EXISTS.getMessage());
    }

    private List<Long> setMethod(Object... args)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = enrollCourseService.getClass()
                .getDeclaredMethod("getEnrolledCoursesJson", getClasses(args));
        method.setAccessible(true);
        return (List<Long>) method.invoke(enrollCourseService, args);
    }

    @Test
    void testGetEnrolledCoursesJson()
            throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {
        //given
        account.setEnrolledCourseJson("");
        //then
        List<Long> result = setMethod(account);
        assertEquals(new ArrayList<>(), result);
    }

    @Test
    void testEnrollCourseEnrolledCoursesJsonFail() throws JsonProcessingException {
        //given
        Course course = new Course();
        course.setId(1L);
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(courseService.getCourseById(ArgumentMatchers.anyLong())).thenReturn(course);
        when(mapper.readValue(anyString(), any(TypeReference.class))).thenReturn(List.of(1L));
        when(mapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);
        //then
        assertThrows(AppException.class,
                () -> enrollCourseService.enrollCourse(1L),
                ErrorCode.COURSE_ENROLL_FAIL.getMessage());
    }

    @Test
    void testGetEnrolledCoursesJsonFail()
            throws JsonProcessingException {
        //given
        account.setEnrolledCourseJson("[]");
        //when
        when(mapper.readValue(anyString(), any(TypeReference.class))).thenThrow(JsonProcessingException.class);
        //then
        assertThrows(InvocationTargetException.class,
                () -> setMethod(account),
                ErrorCode.COURSE_ENROLL_FAIL.getMessage());
    }

    @Test
    void testEnrollCoursePurchasedNull() {
        //given
        account.setPurchasedCourseJson(null);
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        //then
        assertThrows(AppException.class,
                () -> enrollCourseService.enrollCourse(1L),
                ErrorCode.COURSE_ENROLL_FAIL.getMessage());
    }

    @Test
    void testEnrollCoursePurchasedEmpty() {
        //given
        account.setPurchasedCourseJson("");
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        //then
        assertThrows(AppException.class,
                () -> enrollCourseService.enrollCourse(1L),
                ErrorCode.COURSE_ENROLL_FAIL.getMessage());
    }

}