package com.fpt.cursus.controller;

import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.enums.CourseStatus;
import com.fpt.cursus.enums.UserStatus;
import com.fpt.cursus.enums.InstructorStatus;
import com.fpt.cursus.enums.Role;
import com.fpt.cursus.service.CourseService;
import com.fpt.cursus.service.AccountService;
import com.fpt.cursus.util.ApiResUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@Tag(name = "Admin Controller")
public class AdminController {
    private final ApiResUtil apiResUtil;
    private final AccountService accountService;
    private final CourseService courseService;

    public AdminController(ApiResUtil apiResUtil, AccountService accountService, CourseService courseService) {
        this.apiResUtil = apiResUtil;
        this.accountService = accountService;
        this.courseService = courseService;
    }

    @PatchMapping("/admin/verify-instructor")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiRes<Object> verifyAccount(@RequestParam long id, @RequestParam InstructorStatus status) {
        accountService.verifyInstructorById(id, status);
        String successMessage = "Verify instructor successfully.";
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }

    @GetMapping("/admin/view-draft-course")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiRes<Object> viewDraftCourse(@RequestParam(required = false) String sortBy,
                                          @RequestParam(defaultValue = "1", required = false) int offset,
                                          @RequestParam(defaultValue = "10", required = false) int pageSize) {

        return apiResUtil.returnApiRes(null, null, null,
                courseService.getCourseByStatus(CourseStatus.DRAFT, offset, pageSize, sortBy));
    }

    @GetMapping("/admin/view-all-course")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiRes<Object> viewAllCourse(@RequestParam(required = false) String sortBy,
                                        @RequestParam(defaultValue = "1", required = false) int offset,
                                        @RequestParam(defaultValue = "10", required = false) int pageSize){
        return apiResUtil.returnApiRes(null,null,null,
                courseService.getAllCourse(offset,pageSize,sortBy));
    }

    @PatchMapping("/admin/verify-course")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiRes<Object> verifyCourse(@RequestParam long id, @RequestParam CourseStatus status) {
        courseService.verifyCourseById(id, status);
        String successMessage = "Verify course successfully.";
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }

    @GetMapping("/admin/view-verifying-instructor")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiRes<Object> viewVerifyingInstructor(@RequestParam InstructorStatus status) {
        return apiResUtil.returnApiRes(null, null, null,
                accountService.getInstructorByInstStatus(status));
    }
    @GetMapping("/admin/view-instructor-student-list")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiRes<Object> viewList(@RequestParam(required = false) Role role,
                                   @RequestParam(required = false) String sortBy,
                                   @RequestParam(defaultValue = "1", required = false) int offset,
                                   @RequestParam(defaultValue = "10", required = false) int pageSize){
        return apiResUtil.returnApiRes(null,null,null,
                accountService.getListOfStudentAndInstructor(role,offset,pageSize,sortBy));
    }
    @DeleteMapping("/admin/delete-account")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiRes<Object> deleteAccount(@RequestParam String username, @RequestParam UserStatus status) {
        accountService.setStatusAccount(username,status);
        String successMessage = "Update status of account successfully.";
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }

    @PatchMapping("/admin/set-admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiRes<Object> setAdmin(@RequestParam String email) {
        accountService.setAdmin(email);
        String successMessage = "Set admin role successfully.";
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }
}
