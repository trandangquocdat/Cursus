package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.CreateChapterRequest;
import com.fpt.cursus.dto.request.UpdateChapterDto;
import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.entity.Chapter;
import com.fpt.cursus.service.ChapterService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@Tag(name = "Chapter Controller")
public class ChapterController {
    private final ChapterService chapterService;

    @Autowired
    public ChapterController(ChapterService chapterService) {
        this.chapterService = chapterService;
    }

    @PostMapping("/chapter/create")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('INSTRUCTOR')")
    public ResponseEntity<Object> createChapter(@RequestParam Long courseId, @RequestBody @Valid CreateChapterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chapterService.createChapter(courseId, request));
    }

    @DeleteMapping("/chapter/delete")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('INSTRUCTOR')")
    public ResponseEntity<Object> deleteChapter(@RequestParam Long chapterId) {
        return ResponseEntity.ok(chapterService.deleteChapterById(chapterId));
    }

    @PutMapping("/chapter/update-by-id")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('INSTRUCTOR')")
    public ResponseEntity<Object> updateChapter(@RequestParam Long chapterId, @RequestBody @Valid UpdateChapterDto request) {
        return ResponseEntity.ok(chapterService.updateChapter(chapterId, request));
    }

    @GetMapping("/chapter/get-by-id")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('INSTRUCTOR')")
    public ResponseEntity<Object> findChapterById(@RequestParam Long chapterId) {
        return ResponseEntity.ok(chapterService.findChapterById(chapterId));
    }

    @GetMapping("/chapter/get-all")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('INSTRUCTOR')")
    public ResponseEntity<Object> findAll() {
        List<Chapter> chapters = chapterService.findAll();
        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setData(chapters);
        return ResponseEntity.ok(apiRes);
    }

    @GetMapping("/chapter/get-by-course")
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('INSTRUCTOR')")
    public ResponseEntity<Object> findAllByCourseId(@RequestParam Long courseId) {
        return ResponseEntity.ok(chapterService.findAllByCourseId(courseId));
    }

}
