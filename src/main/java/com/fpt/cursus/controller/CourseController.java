package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.CreateCourseDto;
import com.fpt.cursus.dto.request.UpdateCourseDto;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.enums.Category;
import com.fpt.cursus.service.CourseService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@Tag(name = "Course Controller")
public class CourseController {

    private final CourseService courseService;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping(value = "/course/create", consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('INSTRUCTOR')")
    public ResponseEntity<Object> createCourse(@ModelAttribute @Valid CreateCourseDto createCourseDto) {
        Course course = courseService.createCourse(createCourseDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(course);
    }

    @PutMapping(value = "/course/update", consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('INSTRUCTOR')")
    public ResponseEntity<Object> updateCourse(@RequestParam Long id, @ModelAttribute @Valid UpdateCourseDto request) {
        return ResponseEntity.status(HttpStatus.OK).body(courseService.updateCourse(id, request));

    }

    @DeleteMapping("/course/delete")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('INSTRUCTOR')")
    public ResponseEntity<Object> deleteCourse(@RequestParam Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(courseService.deleteCourseById(id));
    }

    @GetMapping("/course/view-all-general")
    public ResponseEntity<Object> viewAllGeneralCourse(@RequestParam(required = false) String sortBy,
                                                       @RequestParam(defaultValue = "1", required = false) int offset,
                                                       @RequestParam(defaultValue = "10", required = false) int pageSize) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(courseService.getAllGeneralCourses(sortBy, offset, pageSize));
    }


    @GetMapping("/course/view-general-by-category")
    public ResponseEntity<Object> viewCourseByCategory(@RequestParam(defaultValue = "ALL") Category category,
                                                       @RequestParam(required = false) String sortBy,
                                                       @RequestParam(defaultValue = "1", required = false) int offset,
                                                       @RequestParam(defaultValue = "10", required = false) int pageSize) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(courseService.getCourseByCategory(category, offset, pageSize, sortBy));
    }

    @GetMapping("/course/view-general-by-name")
    public ResponseEntity<Object> viewCourseByName(@RequestParam String name,
                                                   @RequestParam(required = false) String sortBy,
                                                   @RequestParam(defaultValue = "1", required = false) int offset,
                                                   @RequestParam(defaultValue = "10", required = false) int pageSize) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(courseService.getGeneralCourseByName(name, offset, pageSize, sortBy));
    }

    @GetMapping("/category")
    public ResponseEntity<Object> getAllCategory() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(courseService.getAllCategory());
    }

    @GetMapping("/course/view-general-by-id")
    public ResponseEntity<Object> viewCourseById(@RequestParam Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(courseService.getGeneralCourseById(id));
    }

}
