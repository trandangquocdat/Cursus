//package com.fpt.cursus.util;
//
//import com.fpt.cursus.dto.CourseReqDto;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//
//import java.util.Objects;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class PagingAndSortingUtilTest {
//
//    private CourseReqDto courseReqDto;
//
//    @BeforeEach
//    void setUp() {
//        courseReqDto = new CourseReqDto();
//    }
//
//    @Test
//    void getPageable_withValidSortDirection() {
//        courseReqDto.setSortDirection("ASC");
//        courseReqDto.setPage(1);
//        courseReqDto.setSize(10);
//
//        Pageable result = PagingAndSortingUtil.getPageable(courseReqDto);
//
//        assertEquals(Sort.Direction.ASC, Objects.requireNonNull(result.getSort().getOrderFor("ASC")).getDirection());
//        assertEquals(1, result.getPageNumber());
//        assertEquals(10, result.getPageSize());
//    }
//
//    @Test
//    void getPageable_withInvalidSortDirection() {
//        courseReqDto.setSortDirection("INVALID");
//        courseReqDto.setPage(1);
//        courseReqDto.setSize(10);
//
//        Pageable result = PagingAndSortingUtil.getPageable(courseReqDto);
//
//        assertEquals(Sort.Direction.DESC, Objects.requireNonNull(result.getSort().getOrderFor("INVALID")).getDirection());
//        assertEquals(1, result.getPageNumber());
//        assertEquals(10, result.getPageSize());
//    }
//
//    @Test
//    void getPageable_withNegativePageNumber() {
//        courseReqDto.setSortDirection("ASC");
//        courseReqDto.setPage(-1);
//        courseReqDto.setSize(10);
//
//        assertThrows(IllegalArgumentException.class, () -> PagingAndSortingUtil.getPageable(courseReqDto));
//    }
//
//    @Test
//    void getPageable_withZeroPageSize() {
//        courseReqDto.setSortDirection("ASC");
//        courseReqDto.setPage(1);
//        courseReqDto.setSize(0);
//
//        assertThrows(IllegalArgumentException.class, () -> PagingAndSortingUtil.getPageable(courseReqDto));
//    }
//}