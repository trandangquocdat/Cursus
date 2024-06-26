package com.fpt.cursus.controller;

import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.service.impl.CourseServiceImpl;
import com.fpt.cursus.util.ApiResUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class ProcessController {
    @Autowired
    private ApiResUtil apiResUtil;
    @Autowired
    private CourseServiceImpl courseService;

    @PutMapping("/process")
    public ApiRes<?> addStudiedLesson(@RequestParam Long courserId, @RequestParam Long lessonId) {
        courseService.addStudiedLesson(courserId, lessonId);
        String successMessage = "Add studied lesson successfully";
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }

    @PutMapping("/process/percent-done")
    public ApiRes<?> percentDoneCourse(@RequestParam Long courseId) {
        return apiResUtil.returnApiRes(null, null, null, courseService.percentDoneCourse(courseId));
    }

}
