//package com.fpt.cursus.servive;
//
//import com.fpt.cursus.dto.CourseReqDto;
//import com.fpt.cursus.dto.CourseResDto;
//import com.fpt.cursus.repository.CourseRepo;
//import com.fpt.cursus.service.CourseService;
//import com.fpt.cursus.util.PagingAndSortingUtil;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.Mockito.*;
//
//class CourseServiceTest {
//
//    private CourseService courseService;
//    private CourseRepo courseRepo;
//
//    @BeforeEach
//    void setUp() {
//        courseRepo = mock(CourseRepo.class);
//        courseService = new CourseService(courseRepo);
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
//        Pageable pageable = PagingAndSortingUtil.getPageable(reqDto);
//        when(courseRepo.searchActiveCourses(reqDto.getKeyword(), PageRequest.of(1, 20))).thenReturn(Optional.of(Page.empty()));
//
//        Page<CourseResDto> result = courseService.getCourses(reqDto);
//
//        assertNotNull(result);
//        verify(courseRepo, times(1)).searchActiveCourses(reqDto.getKeyword(), PageRequest.of(1, 20));
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
//        Pageable pageable = PagingAndSortingUtil.getPageable(reqDto);
//        when(courseRepo.searchActiveCourses(reqDto.getKeyword(), PageRequest.of(1, 20))).thenReturn(Optional.of(Page.empty()));
//
//        Page<CourseResDto> result = courseService.getCourses(reqDto);
//
//        assertNotNull(result);
//        verify(courseRepo, times(1)).searchActiveCourses(reqDto.getKeyword(), PageRequest.of(1, 20));
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
//        Pageable pageable = PagingAndSortingUtil.getPageable(reqDto);
//        when(courseRepo.searchActiveCourses(reqDto.getKeyword(), PageRequest.of(1, 20))).thenReturn(Optional.of(Page.empty()));
//
//        Page<CourseResDto> result = courseService.getCourses(reqDto);
//
//        assertNotNull(result);
//        verify(courseRepo, times(1)).searchActiveCourses(reqDto.getKeyword(), PageRequest.of(1, 20));
//    }
//}