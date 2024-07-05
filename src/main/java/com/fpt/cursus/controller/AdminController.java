package com.fpt.cursus.controller;

import com.fpt.cursus.enums.CourseStatus;
import com.fpt.cursus.enums.UserStatus;
import com.fpt.cursus.enums.InstructorStatus;
import com.fpt.cursus.enums.Role;
import com.fpt.cursus.service.CourseService;
import com.fpt.cursus.service.AccountService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@Tag(name = "Admin Controller")
public class AdminController {
    private final AccountService accountService;
    private final CourseService courseService;

    public AdminController(AccountService accountService, CourseService courseService) {

        this.accountService = accountService;
        this.courseService = courseService;
    }

    @PatchMapping("/admin/verify-instructor")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Object> verifyAccount(@RequestParam long id, @RequestParam InstructorStatus status) {
        return ResponseEntity.ok(accountService.verifyInstructorById(id, status));
    }

    @GetMapping("/admin/view-draft-course")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Object> viewDraftCourse(@RequestParam(required = false) String sortBy,
                                          @RequestParam(defaultValue = "1", required = false) int offset,
                                          @RequestParam(defaultValue = "10", required = false) int pageSize) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(courseService.getCourseByStatus(CourseStatus.DRAFT, offset, pageSize, sortBy));
    }

    @GetMapping("/admin/view-all-course")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Object> viewAllCourse(@RequestParam(required = false) String sortBy,
                                        @RequestParam(defaultValue = "1", required = false) int offset,
                                        @RequestParam(defaultValue = "10", required = false) int pageSize) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(courseService.getAllCourse(offset, pageSize, sortBy));
    }

    @PatchMapping("/admin/verify-course")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Object> verifyCourse(@RequestParam long id, @RequestParam CourseStatus status) {
        return ResponseEntity.ok(courseService.verifyCourseById(id, status));
    }

    @GetMapping("/admin/view-verifying-instructor")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Object> viewVerifyingInstructor(@RequestParam InstructorStatus status) {
        return ResponseEntity.ok(accountService.getInstructorByInstStatus(status));
    }

    @GetMapping("/admin/view-instructor-student-list")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Object> viewList(@RequestParam(required = false) Role role,
                                   @RequestParam(required = false) String sortBy,
                                   @RequestParam(defaultValue = "1", required = false) int offset,
                                   @RequestParam(defaultValue = "10", required = false) int pageSize) {
        return ResponseEntity.ok(accountService.getListOfStudentAndInstructor(role, offset, pageSize, sortBy));
    }

    @DeleteMapping("/admin/set-status-account")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Object> setStatusAccount(@RequestParam String username, @RequestParam UserStatus status) {
        return ResponseEntity.ok(accountService.setStatusAccount(username, status));
    }

    @PatchMapping("/admin/set-admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Object> setAdmin(@RequestParam String email) {
        return ResponseEntity.ok(accountService.setAdmin(email));
    }
}
