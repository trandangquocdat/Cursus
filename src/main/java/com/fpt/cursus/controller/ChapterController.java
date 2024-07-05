package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.CreateChapterRequest;
import com.fpt.cursus.dto.request.UpdateChapterDto;
import com.fpt.cursus.service.ChapterService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@Tag(name = "Chapter Controller")
public class ChapterController {
    private final ChapterService chapterService;

    public ChapterController(ChapterService chapterService) {
        this.chapterService = chapterService;
    }

    @PostMapping("/chapter/create")
    public ResponseEntity<Object> createChapter(@RequestParam Long courseId, @RequestBody @Valid CreateChapterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chapterService.createChapter(courseId, request));
    }
    @DeleteMapping("/chapter/delete")
    public ResponseEntity<Object> deleteChapter(@RequestParam Long chapterId) {
        return ResponseEntity.ok(chapterService.deleteChapterById(chapterId));
    }
    @PutMapping("/chapter/update-by-id")
    public ResponseEntity<Object> updateChapter(@RequestParam Long chapterId, @RequestBody @Valid UpdateChapterDto request) {
        return ResponseEntity.ok(chapterService.updateChapter(chapterId, request));
    }

    @GetMapping("/chapter/get-by-id")
    public ResponseEntity<Object> findChapterById(@RequestParam Long chapterId) {
        return ResponseEntity.ok(chapterService.findChapterById(chapterId));
    }

    @GetMapping("/chapter/get-all")
    public ResponseEntity<Object> findAll() {
        return ResponseEntity.ok(chapterService.findAll());
    }

    @GetMapping("/chapter/get-by-course")
    public ResponseEntity<Object> findAllByCourseId(@RequestParam Long courseId) {
        return ResponseEntity.ok(chapterService.findAllByCourseId(courseId));
    }

}
