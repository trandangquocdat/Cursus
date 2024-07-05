package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.CreateCourseDto;
import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.enums.Category;
import com.fpt.cursus.service.CourseService;
import com.fpt.cursus.util.ApiResUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@Tag(name = "Course Controller")
public class CourseController {

    private final CourseService courseService;
    private final ApiResUtil apiResUtil;

    public CourseController(CourseService courseService, ApiResUtil apiResUtil) {
        this.courseService = courseService;
        this.apiResUtil = apiResUtil;
    }

    @PostMapping("/course/create")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('INSTRUCTOR')")
    public ApiRes<Object> createCourse(@RequestBody CreateCourseDto createCourseDto) {
        return apiResUtil.returnApiRes(null, null, null,
                courseService.createCourse(createCourseDto));
    }

    @PutMapping("/course/update")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('INSTRUCTOR')")
    public ApiRes<Object> updateCourse(@RequestParam Long id, @RequestBody CreateCourseDto createCourseDto) {
        courseService.updateCourse(id, createCourseDto);
        String successMessage = "Update course successfully!";
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }
    @DeleteMapping("/course/delete")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('INSTRUCTOR')")
    public ApiRes<Object> deleteCourse(@RequestParam Long id) {
        String successMessage = "Delete course successfully!";
        courseService.deleteCourseById(id);
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }
    @PostMapping("/course/add-to-wishlist")
    public ApiRes<Object> addToWishList(@RequestParam List<Long> id) {
        courseService.addToWishList(id);
        String successMessage = "Add course to wishlist successfully!";
        return apiResUtil.returnApiRes(null, null, successMessage,null);
    }
    @DeleteMapping("/course/remove-from-wishlist")
    public ApiRes<Object> removeFromWishList(@RequestParam Long id) {
        courseService.removeFromWishList(id);
        String successMessage = "Remove course from wishlist successfully!";
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }
    @GetMapping("/course/view-wishlist")
    public ApiRes<Object> viewWishList(@RequestParam(required = false) String sortBy,
                                       @RequestParam(defaultValue = "1", required = false) int offset,
                                       @RequestParam(defaultValue = "10", required = false) int pageSize) {
        return apiResUtil.returnApiRes(null, null, null,
                courseService.getWishListCourses(offset, pageSize,sortBy ));
    }
    @GetMapping("/course/view-my-course")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('INSTRUCTOR') ")
    public ApiRes<Object> viewMyCourse(@RequestParam(required = false) String sortBy,
                                       @RequestParam(defaultValue = "1", required = false) int offset,
                                       @RequestParam(defaultValue = "10", required = false) int pageSize){

        return apiResUtil.returnApiRes(null, null, null,
                courseService.getCourseByCreatedBy(offset,pageSize , sortBy));
    }
    @GetMapping("/course/view-all-general-course")
    public ApiRes<Object> viewAllGeneralCourse(@RequestParam(required = false) String sortBy,
                                  @RequestParam(defaultValue = "1", required = false) int offset,
                                  @RequestParam(defaultValue = "10", required = false) int pageSize) {
        return apiResUtil.returnApiRes(null, null, null,
                courseService.getAllGeneralCourses(sortBy, offset, pageSize));
    }

    @GetMapping("/course/view-general-enrolled_course")
    public ApiRes<Object> viewEnrolledCourses(@RequestParam(required = false) String sortBy,
                                        @RequestParam(defaultValue = "1", required = false) int offset,
                                        @RequestParam(defaultValue = "10", required = false) int pageSize) {

        return apiResUtil.returnApiRes(null, null, null,
                courseService.getGeneralEnrolledCourses(sortBy, offset, pageSize));
    }

    @GetMapping("/course/view-detail-enrolled_course")
    public ApiRes<Object> viewDetailEnrolledCourses(@RequestParam(required = false) String sortBy,
                                              @RequestParam(defaultValue = "1", required = false) int offset,
                                              @RequestParam(defaultValue = "10", required = false) int pageSize) {
        return apiResUtil.returnApiRes(null, null, null,
                courseService.getDetailEnrolledCourses(sortBy, offset, pageSize));
    }

    @GetMapping("/course/view-general-course-by-category")
    public ApiRes<Object> viewCourseByCategory(@RequestParam(defaultValue = "ALL") Category category,
                                         @RequestParam(required = false) String sortBy,
                                         @RequestParam(defaultValue = "1", required = false) int offset,
                                         @RequestParam(defaultValue = "10", required = false) int pageSize) {

        return apiResUtil.returnApiRes(null, null, null,
                courseService.getCourseByCategory(category,offset, pageSize, sortBy));
    }
    @GetMapping("/course/view-general-course-by-name")
    public ApiRes<Object> viewCourseByName(@RequestParam String name,
                                           @RequestParam(required = false) String sortBy,
                                           @RequestParam(defaultValue = "1", required = false) int offset,
                                           @RequestParam(defaultValue = "10", required = false) int pageSize){

        return apiResUtil.returnApiRes(null, null, null,
                courseService.getGeneralCourseByName(name,offset, pageSize, sortBy));
    }

}
