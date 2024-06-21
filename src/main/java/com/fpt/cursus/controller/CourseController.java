package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.CreateCourseDto;
import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.enums.status.CourseStatus;
import com.fpt.cursus.service.CourseService;
import com.fpt.cursus.util.ApiResUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class CourseController {
    @Autowired
    private CourseService courseService;
    @Autowired
    private ApiResUtil apiResUtil;

    @PostMapping("/couse/create")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('INSTRUCTOR')")
    public ApiRes<?> createCourse(@RequestBody CreateCourseDto createCourseDto) {
        return apiResUtil.returnApiRes(null, null, null, courseService.createCourse(createCourseDto));
    }

    @PutMapping("/course/update")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('INSTRUCTOR')")
    public ApiRes<?> updateCourse(@RequestParam Long id, @RequestBody CreateCourseDto createCourseDto) {
        courseService.updateCourse(id, createCourseDto);
        String successMessage = "Update course successfully!";
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }

    @GetMapping("/course/get-draft-course")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('INSTRUCTOR')")
    public ApiRes<?> viewDraftCourse() {
        return apiResUtil.returnApiRes(null, null,null,courseService.findCourseByStatus(CourseStatus.DRAFT));
    }

    @DeleteMapping("/course/delete")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('INSTRUCTOR')")
    public ApiRes<?> deleteCourse(@RequestParam Long id) {
        String successMessage = "Delete course successfully!";
        courseService.deleteCourseById(id);
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }

    @GetMapping("/course/get-all-course")
    public ApiRes<?> findAllCourse(@RequestParam(required = false) String sortBy, @RequestParam(defaultValue = "1", required = false) int offset, @RequestParam(defaultValue = "10", required = false) int pageSize) {
        return apiResUtil.returnApiRes(null, null, null, courseService.getAllCourse(sortBy, offset, pageSize));
    }
    @GetMapping("/course/get-enrolled_course")
    public ApiRes<?> getEnrolledCourses() {
        List<Course> enrolledCourse = courseService.getEnrolledCourses();
        return apiResUtil.returnApiRes(null, null, null, enrolledCourse);

    }
}
