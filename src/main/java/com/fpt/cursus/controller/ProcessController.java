package com.fpt.cursus.controller;

import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.service.CourseService;
import com.fpt.cursus.util.ApiResUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@Tag(name = "Process Controller")
public class ProcessController {
    private final ApiResUtil apiResUtil;
    private final CourseService courseService;

    public ProcessController(ApiResUtil apiResUtil, CourseService courseService) {
        this.apiResUtil = apiResUtil;
        this.courseService = courseService;
    }

    @PutMapping("/process/add-studied-lesson")
    public ApiRes<Object> addStudiedLesson(@RequestParam Long lessonId) {
        courseService.addStudiedLesson(lessonId);
        String successMessage = "Add studied lesson successfully";
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }

    @PutMapping("/process/percent-done")
    public ApiRes<Object> percentDoneCourse(@RequestParam Long courseId) {
        return apiResUtil.returnApiRes(null, null, null, courseService.percentDoneCourse(courseId));
    }

    @GetMapping("/process/view-all-studied-lesson")
    public ApiRes<Object> viewAllStudiedLesson() {
        return apiResUtil.returnApiRes(null, null, null, courseService.getAllStudiedCourses());
    }
    @GetMapping("/process/view-checkpoint")
    public ApiRes<Object> viewLastStudiedLesson() {
        return apiResUtil.returnApiRes(null, null, null, courseService.getCheckPoint());
    }

}
