package com.fpt.cursus.controller;

import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.enums.status.CourseStatus;
import com.fpt.cursus.service.CourseService;
import com.fpt.cursus.service.UserService;
import com.fpt.cursus.util.ApiResUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class AdminController {
    @Autowired
    private ApiResUtil apiResUtil;
    @Autowired
    private UserService userService;
    @Autowired
    private CourseService courseService;

    @PatchMapping("/admin/verify-instructor")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiRes<?> verifyAccount(@RequestParam long id) {
        userService.verifyInstructorById(id);
        String successMessage = "Verify instructor successfully.";
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }

    @PatchMapping("/admin/verify-course")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiRes<?> verifyCourse(@RequestParam long id) {
        courseService.verifyCourseById(id);
        String successMessage = "Verify course successfully.";
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }

    @GetMapping("/admin/get-verifying-instructor")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiRes<?> getVerifyingInstructor() {
        return apiResUtil.returnApiRes(null, null, null, userService.getVerifyingInstructor());
    }

    @DeleteMapping("/auth/delete-account")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiRes<?> deleteAccount(@RequestParam String username) {
        userService.deleteAccount(username);
        String successMessage = "Delete account successfully.";
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }
}
