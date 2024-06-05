//package com.fpt.cursus.repository;
//
//import com.fpt.cursus.dto.CourseResDto;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.Mockito.*;
//
//class CourseRepoTest {
//
//    private CourseRepo courseRepo;
//
//    @BeforeEach
//    void setUp() {
//        courseRepo = mock(CourseRepo.class);
//    }
//
//    @Test
//    void searchActiveCourses_withValidKeyword() {
//        String keyword = "java";
//        Pageable pageable = PageRequest.of(0, 10);
//
//        when(courseRepo.searchActiveCourses(keyword, PageRequest.of(1, 20))).thenReturn(Optional.of(Page.empty()));
//
//        Optional<Page<CourseResDto>> result = courseRepo.searchActiveCourses(keyword, PageRequest.of(1, 20));
//
//        assertTrue(result.isPresent());
//        verify(courseRepo, times(1)).searchActiveCourses(keyword, PageRequest.of(1, 20));
//    }
//
//    @Test
//    void searchActiveCourses_withEmptyKeyword() {
//        String keyword = "";
//        Pageable pageable = PageRequest.of(0, 10);
//
//        when(courseRepo.searchActiveCourses(keyword, PageRequest.of(1, 20))).thenReturn(Optional.of(Page.empty()));
//
//        Optional<Page<CourseResDto>> result = courseRepo.searchActiveCourses(keyword, PageRequest.of(1, 20));
//
//        assertTrue(result.isPresent());
//        verify(courseRepo, times(1)).searchActiveCourses(keyword, PageRequest.of(1, 20));
//    }
//
//    @Test
//    void searchActiveCourses_withNullKeyword() {
//        String keyword = null;
//        Pageable pageable = PageRequest.of(0, 10);
//
//        when(courseRepo.searchActiveCourses(keyword, PageRequest.of(1, 20))).thenReturn(Optional.of(Page.empty()));
//
//        Optional<Page<CourseResDto>> result = courseRepo.searchActiveCourses(keyword, PageRequest.of(1, 20));
//
//        assertTrue(result.isPresent());
//        verify(courseRepo, times(1)).searchActiveCourses(keyword, PageRequest.of(1, 20));
//    }
//}