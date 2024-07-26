package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.CheckAnswerReq;
import com.fpt.cursus.service.QuizService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@Tag(name = "Quiz Controller")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/quiz")
    public ResponseEntity<Object> getQuizById(@RequestParam("id") Long id) {
        return ResponseEntity.ok(quizService.getQuizById(id));
    }

    @GetMapping("/quiz/answer")
    @PreAuthorize("hasAnyAuthority('ADMIN','INSTRUCTOR')")
    public ResponseEntity<Object> getAnswerById(@RequestParam("id") Long id) {
        return ResponseEntity.ok(quizService.getAnswerById(id));
    }

    @PostMapping(value = "/quiz", consumes = "multipart/form-data")
    public ResponseEntity<Object> createQuiz(@RequestParam("file") MultipartFile file, @RequestParam Long courseId,
                                             @RequestParam String name) {
        return ResponseEntity.ok(quizService.createQuiz(file, courseId, name));
    }

    @PutMapping("/quiz/scoring")
    public ResponseEntity<Object> scoringQuiz(@RequestBody CheckAnswerReq request) {
        return ResponseEntity.ok(quizService.scoringQuiz(request));
    }

}
