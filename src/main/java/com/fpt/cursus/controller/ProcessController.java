package com.fpt.cursus.controller;

import com.fpt.cursus.service.CourseService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@Tag(name = "Process Controller")
public class ProcessController {
    private final CourseService courseService;

    public ProcessController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PutMapping("/process/add-studied-lesson")
    public ResponseEntity<Object> addStudiedLesson(@RequestParam Long lessonId) {
        return ResponseEntity.ok(courseService.addStudiedLesson(lessonId));
    }

    @PutMapping("/process/percent-done")
    public ResponseEntity<Object> percentDoneCourse(@RequestParam Long courseId) {
        return ResponseEntity.ok(courseService.percentDoneCourse(courseId));
    }

    @GetMapping("/process/view-all-studied-lesson")
    public ResponseEntity<Object> viewAllStudiedLesson() {
        return ResponseEntity.ok(courseService.getAllStudiedCourses());
    }
    @GetMapping("/process/view-checkpoint")
    public ResponseEntity<Object> viewLastStudiedLesson() {
        return ResponseEntity.ok(courseService.getCheckPoint());
    }

}
