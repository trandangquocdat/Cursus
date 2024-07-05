package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.CreateLessonDto;
import com.fpt.cursus.service.LessonService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@Tag(name = "Lesson Controller")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {

        this.lessonService = lessonService;
    }

    @PostMapping("/lesson/create")
    public ResponseEntity<Object> createLesson(@RequestParam Long chapterId, @RequestBody @Valid CreateLessonDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.createLesson(chapterId, request));
    }

    @PutMapping("/lesson/update")
    public ResponseEntity<Object> updateLesson(@RequestParam Long lessonId, @RequestBody @Valid CreateLessonDto request) {
        return ResponseEntity.status(HttpStatus.OK).body(lessonService.updateLesson(lessonId, request));
    }

    @DeleteMapping("/lesson/delete")
    public ResponseEntity<Object> deleteLesson(@RequestParam Long lessonId) {
        return ResponseEntity.status(HttpStatus.OK).body(lessonService.deleteLessonById(lessonId));
    }

    @GetMapping("/lesson/get-all")
    public ResponseEntity<Object> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(lessonService.findAll());
    }
    @GetMapping("/lesson/get-by-chapter")
    public ResponseEntity<Object> findById(@RequestParam  Long chapterId) {
        return ResponseEntity.status(HttpStatus.OK).body(lessonService.findAllByChapterId(chapterId));
    }
}
