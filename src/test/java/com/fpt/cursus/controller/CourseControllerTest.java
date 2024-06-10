//package com.fpt.cursus.controller;
//
//import com.fpt.cursus.dto.response.ApiRes;
//import com.fpt.cursus.dto.CourseReqDto;
//import com.fpt.cursus.dto.CourseResDto;
//import com.fpt.cursus.service.CourseService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.data.domain.Page;
//import org.springframework.http.HttpStatus;
//
//import static org.mockito.Mockito.*;
//import static org.junit.jupiter.api.Assertions.*;
//
//class CourseControllerTest {
//
//    private CourseController courseController;
//    private CourseService courseService;
//
//    @BeforeEach
//    void setUp() {
//        courseService = mock(CourseService.class);
//        courseController = new CourseController(courseService);
//    }
//
//    @Test
//    void getCourses_withValidRequest() {
//        CourseReqDto reqDto = new CourseReqDto();
//        reqDto.setKeyword("java");
//        reqDto.setPage(1);
//        reqDto.setSize(10);
//        reqDto.setSortDirection("ASC");
//
//        when(courseService.getCourses(reqDto)).thenReturn(Page.empty());
//
//        ApiRes<Page<CourseResDto>> result = courseController.getCourses(reqDto);
//
//        assertTrue(result.isStatus());
//        assertEquals(HttpStatus.OK.value(), result.getCode());
//        assertEquals("Available Courses.", result.getMessage());
//        assertNotNull(result.getResult());
//        verify(courseService, times(1)).getCourses(reqDto);
//    }
//
//    @Test
//    void getCourses_withEmptyKeyword() {
//        CourseReqDto reqDto = new CourseReqDto();
//        reqDto.setKeyword("");
//        reqDto.setPage(1);
//        reqDto.setSize(10);
//        reqDto.setSortDirection("ASC");
//
//        when(courseService.getCourses(reqDto)).thenReturn(Page.empty());
//
//        ApiRes<Page<CourseResDto>> result = courseController.getCourses(reqDto);
//
//        assertTrue(result.isStatus());
//        assertEquals(HttpStatus.OK.value(), result.getCode());
//        assertEquals("Available Courses.", result.getMessage());
//        assertNotNull(result.getResult());
//        verify(courseService, times(1)).getCourses(reqDto);
//    }
//
//    @Test
//    void getCourses_withNullKeyword() {
//        CourseReqDto reqDto = new CourseReqDto();
//        reqDto.setKeyword(null);
//        reqDto.setPage(1);
//        reqDto.setSize(10);
//        reqDto.setSortDirection("ASC");
//
//        when(courseService.getCourses(reqDto)).thenReturn(Page.empty());
//
//        ApiRes<Page<CourseResDto>> result = courseController.getCourses(reqDto);
//
//        assertTrue(result.isStatus());
//        assertEquals(HttpStatus.OK.value(), result.getCode());
//        assertEquals("Available Courses.", result.getMessage());
//        assertNotNull(result.getResult());
//        verify(courseService, times(1)).getCourses(reqDto);
//    }
//}