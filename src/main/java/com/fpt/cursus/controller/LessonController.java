package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.CreateLessonDto;
import com.fpt.cursus.service.LessonService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@Tag(name = "Lesson Controller")
public class LessonController {

    private final LessonService lessonService;


    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @PostMapping(value = "/lesson/create", consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('INSTRUCTOR')")
    public ResponseEntity<Object> createLesson(@RequestParam Long chapterId,
                                               @ModelAttribute @Valid CreateLessonDto request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.createLesson(chapterId, request));
    }

    @PostMapping("/lesson/upload-excel")
    public ResponseEntity<List<String>> uploadExcelFile(@RequestParam Long chapterId,
                                                        @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok().body(lessonService.uploadLessonFromExcel(chapterId, file));
    }

    @PutMapping("/lesson/update")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('INSTRUCTOR')")
    public ResponseEntity<Object> updateLesson(@RequestParam Long lessonId,
                                               @ModelAttribute @Valid CreateLessonDto request) {
        return ResponseEntity.status(HttpStatus.OK).body(lessonService.updateLesson(lessonId, request));
    }

    @DeleteMapping("/lesson/delete")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('INSTRUCTOR')")
    public ResponseEntity<Object> deleteLesson(@RequestParam Long lessonId) {
        return ResponseEntity.status(HttpStatus.OK).body(lessonService.deleteLessonById(lessonId));
    }

    @GetMapping("/lesson/get-all")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('INSTRUCTOR')")
    public ResponseEntity<Object> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(lessonService.findAll());
    }

    @GetMapping("/lesson/get-by-chapter")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('INSTRUCTOR')")
    public ResponseEntity<Object> findById(@RequestParam Long chapterId) {
        return ResponseEntity.status(HttpStatus.OK).body(lessonService.findAllByChapterId(chapterId));
    }
}
