package com.fpt.cursus.controller;

import com.fpt.cursus.dto.object.StudiedCourse;
import com.fpt.cursus.dto.response.CustomAccountResDto;
import com.fpt.cursus.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ProcessControllerTest {

    @Mock
    private CourseService courseService;

    @InjectMocks
    private ProcessController processController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addStudiedLesson_ReturnsOk() {
        long lessonId = 1L;
        CustomAccountResDto expectedResult = new CustomAccountResDto(); // Mock expected result
        ResponseEntity<CustomAccountResDto> expectedResponse = new ResponseEntity<>(expectedResult, HttpStatus.OK);
        when(courseService.addStudiedLesson(lessonId)).thenReturn(expectedResponse.getBody());

        ResponseEntity<Object> response = processController.addStudiedLesson(lessonId);

        verify(courseService, times(1)).addStudiedLesson(lessonId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody());
    }

    @Test
    void percentDoneCourse_ReturnsOk() {
        long courseId = 1L;
        double expectedResult = 0.75; // Mock expected result
        when(courseService.percentDoneCourse(courseId)).thenReturn(expectedResult);

        ResponseEntity<Object> response = processController.percentDoneCourse(courseId);

        verify(courseService, times(1)).percentDoneCourse(courseId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody());
    }

    @Test
    void viewAllStudiedLesson_ReturnsOk() {
        List<StudiedCourse> expectedResult = new ArrayList<>(); // Mock expected result
        when(courseService.getAllStudiedCourses()).thenReturn(expectedResult);

        ResponseEntity<Object> response = processController.viewAllStudiedLesson();

        verify(courseService, times(1)).getAllStudiedCourses();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody());
    }

    @Test
    void viewLastStudiedLesson_ReturnsOk() {
        StudiedCourse expectedResult = new StudiedCourse(); // Mock expected result
        when(courseService.getCheckPoint()).thenReturn(expectedResult);

        ResponseEntity<Object> response = processController.viewLastStudiedLesson();

        verify(courseService, times(1)).getCheckPoint();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody());
    }
}
