package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.CreateCourseDto;
import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.service.CourseService;
import com.fpt.cursus.util.ApiResUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class CourseController {
    @Autowired
    private CourseService courseService;
    @Autowired
    private ApiResUtil apiResUtil;

    @PostMapping("/create-course")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('INSTRUCTOR')")
    public ApiRes<?> createCourse(@RequestBody CreateCourseDto createCourseDto) {
        String successMessage = "Create course successfully!";
        return apiResUtil.returnApiRes(true, HttpStatus.CREATED.value(), successMessage, courseService.createCourse(createCourseDto));
    }
    @PutMapping("/update-course")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('INSTRUCTOR')")
    public ApiRes<?> updateCourse(@RequestParam Long id, @RequestBody CreateCourseDto createCourseDto) {
        String successMessage = "Update course successfully!";
        return apiResUtil.returnApiRes(true, HttpStatus.OK.value(), successMessage, courseService.updateCourse(id, createCourseDto));
    }

    @DeleteMapping("/delete-course")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiRes<?> deleteCourse(@RequestParam Long id) {
        String successMessage = "Delete course successfully!";
        courseService.deleteCourseById(id);
        return apiResUtil.returnApiRes(true, HttpStatus.OK.value(), successMessage, null);
    }

    @GetMapping("/find-all-course-pagination")
    public ApiRes<?> findAllCourse(@RequestParam int offset, @RequestParam int pageSize) {
        String successMessage = "Get course successfully!";
        return apiResUtil.returnApiRes(true, HttpStatus.OK.value(), successMessage, courseService.findAllCourseWithPagination(offset, pageSize));
    }

    @GetMapping("/find-all-course-pagination-sort")
    public ApiRes<?> findAllCourse(@RequestParam String sortBy, @RequestParam int offset, @RequestParam int pageSize) {
        String successMessage = "Get course successfully!";
        return apiResUtil.returnApiRes(true, HttpStatus.OK.value(), successMessage, courseService.findAllCourseWithPaginationAndSort(sortBy, offset, pageSize));
    }
}
