package com.fpt.cursus.service;


import com.fpt.cursus.dto.request.CreateCourseDto;
import com.fpt.cursus.dto.request.UpdateCourseDto;
import com.fpt.cursus.dto.response.GeneralCourse;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.enums.Category;
import com.fpt.cursus.enums.CourseStatus;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.CourseRepo;
import com.fpt.cursus.service.impl.AccountServiceImpl;
import com.fpt.cursus.service.impl.CourseServiceImpl;
import com.fpt.cursus.util.AccountUtil;
import com.fpt.cursus.util.FileUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @Mock
    private CourseRepo courseRepo;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private FileUtil fileUtil;

    @Mock
    private FileService fileService;

    @Mock
    private LessonService lessonService;

    @Mock
    private Account account;

    @Mock
    private AccountUtil accountUtil;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Course sampleCourse;
    private CreateCourseDto sampleCreateCourseDto;
    private UpdateCourseDto sampleUpdateCourseDto;
    private Account mockAccount;


    @BeforeEach
    void setUp() {

        accountUtil = new AccountUtil();
        account = new Account();

        sampleCourse = new Course();
        sampleCourse.setId(1L);
        sampleCourse.setName("Sample Course");
        sampleCourse.setPrice(500000);
        sampleCourse.setStatus(CourseStatus.DRAFT);
        sampleCourse.setStatus(CourseStatus.ACTIVE);


//        sampleCreateCourseDto = new CreateCourseDto();
//        sampleCreateCourseDto.setName("New Course");
//        sampleCreateCourseDto.setPrice(200000.0);
//        sampleCreateCourseDto.setPictureLink(null);
//
//        sampleUpdateCourseDto = new UpdateCourseDto();
//        sampleUpdateCourseDto.setName("Updated Course");
//        sampleUpdateCourseDto.setPrice(300000.0);
//        sampleUpdateCourseDto.setPictureLink(null);
    }

    @Test
    void testDeleteCourseById_Success() {
        //Setup
        Course deleteCourse = new Course();
        //When
        when(courseRepo.findById(anyLong())).thenReturn(Optional.of(deleteCourse));

        //Then

    }

    @Test
    void testDeleteCourseById_CourseNotFound() {
        // Mocking behavior for courseRepo.findById() to return empty
        when(courseRepo.findById(1L)).thenReturn(Optional.empty());

        // Test and assertion
        AppException exception = assertThrows(AppException.class,
                () -> courseService.deleteCourseById(1L));
        assertEquals(ErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());

        // Verify interactions
        verify(courseRepo, times(1)).findById(1L);
        verify(courseRepo, never()).save(any(Course.class));
    }

    @Test
    void testUpdateCourse_CourseNotFound() {
        //when
        when(courseRepo.findById(1L)).thenReturn(java.util.Optional.empty());

        //Then
        AppException exception = assertThrows(AppException.class,
                () -> courseService.updateCourse(1L, sampleUpdateCourseDto));
        assertEquals(ErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());
        verify(courseRepo, never()).save(any());
    }

    @Test
    void testGetCourseById_Success() {
        //When
        when(courseRepo.findById(1L)).thenReturn(java.util.Optional.ofNullable(sampleCourse));

        // Test
        Course foundCourse = courseService.getCourseById(1L);

        //Then
        assertNotNull(foundCourse);
        assertEquals(sampleCourse.getName(), foundCourse.getName());
        assertEquals(sampleCourse.getPrice(), foundCourse.getPrice());
        assertEquals(sampleCourse.getStatus(), foundCourse.getStatus());
    }

    @Test
    void testGetCourseById_CourseNotFound() {
        //When
        when(courseRepo.findById(1L)).thenReturn(java.util.Optional.empty());

        //Then
        AppException exception = assertThrows(AppException.class, () -> courseService.getCourseById(1L));
        assertEquals(ErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());
    }



}
