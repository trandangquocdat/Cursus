package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.CreateCourseDto;
import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.dto.response.GeneralCourse;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.enums.status.CourseStatus;
import com.fpt.cursus.enums.type.Category;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.service.CourseService;
import com.fpt.cursus.util.ApiResUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
        return apiResUtil.returnApiRes(null, null, null,
                courseService.createCourse(createCourseDto));
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
    public ApiRes<?> viewDraftCourse(@RequestParam(required = false) String sortBy,
                                     @RequestParam(defaultValue = "0", required = false) int offset,
                                     @RequestParam(defaultValue = "10", required = false) int pageSize) {
        return apiResUtil.returnApiRes(null, null, null,
                courseService.findCourseByStatus(CourseStatus.DRAFT, offset, pageSize, sortBy));
    }

    @DeleteMapping("/course/delete")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('INSTRUCTOR')")
    public ApiRes<?> deleteCourse(@RequestParam Long id) {
        String successMessage = "Delete course successfully!";
        courseService.deleteCourseById(id);
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }

    @GetMapping("/course/get-all-course")
    public ApiRes<?> getAllCourse(@RequestParam(required = false) String sortBy,
                                  @RequestParam(defaultValue = "0", required = false) int offset,
                                  @RequestParam(defaultValue = "10", required = false) int pageSize) {
        return apiResUtil.returnApiRes(null, null, null,
                courseService.getAllGeneralCourses(sortBy, offset, pageSize));
    }

    @GetMapping("/course/get-general-enrolled_course")
    public ApiRes<?> getEnrolledCourses(@RequestParam(required = false) String sortBy,
                                        @RequestParam(defaultValue = "0", required = false) int offset,
                                        @RequestParam(defaultValue = "10", required = false) int pageSize) {

        return apiResUtil.returnApiRes(null, null, null,
                courseService.getGeneralEnrolledCourses(sortBy, offset, pageSize));
    }

    @GetMapping("/course/get-detail-enrolled_course")
    public ApiRes<?> getDetailEnrolledCourses(@RequestParam(required = false) String sortBy,
                                              @RequestParam(defaultValue = "0", required = false) int offset,
                                              @RequestParam(defaultValue = "10", required = false) int pageSize) {
        return apiResUtil.returnApiRes(null, null, null,
                courseService.getDetailEnrolledCourses(sortBy, offset, pageSize));
    }

    @GetMapping("/course/get-by-category")
    public ApiRes<?> getCourseByCategory(@RequestParam(defaultValue = "ALL") Category category,
                                         @RequestParam(required = false) String sortBy,
                                         @RequestParam(defaultValue = "0", required = false) int offset,
                                         @RequestParam(defaultValue = "10", required = false) int pageSize) {

        return apiResUtil.returnApiRes(null, null, null,
                courseService.findCourseByCategory(category,offset, pageSize, sortBy));
    }

}
