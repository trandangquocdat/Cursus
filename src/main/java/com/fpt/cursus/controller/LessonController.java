package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.CreateLessonDto;
import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.service.LessonService;
import com.fpt.cursus.util.ApiResUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@Tag(name = "Lesson Controller")
public class LessonController {
    private final ApiResUtil apiResUtil;
    private final LessonService lessonService;

    public LessonController(ApiResUtil apiResUtil, LessonService lessonService) {
        this.apiResUtil = apiResUtil;
        this.lessonService = lessonService;
    }

    @PostMapping("/lesson/create")
    public ApiRes<?> createLesson(@RequestParam Long chapterId, @RequestBody @Valid CreateLessonDto request) {
        return apiResUtil.returnApiRes(null, null, null,
                lessonService.createLesson(chapterId, request));
    }

    @PutMapping("/lesson/update")
    public ApiRes<?> updateLesson(@RequestParam Long lessonId, @RequestBody @Valid CreateLessonDto request) {
        lessonService.updateLesson(lessonId, request);
        String successMessage = "Update chapter successfully!";
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }

    @DeleteMapping("/lesson/delete")
    public ApiRes<?> deleteLesson(@RequestParam Long lessonId) {
        lessonService.deleteLessonById(lessonId);
        String successMessage = "Delete chapter successfully!";
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }

    @GetMapping("/lesson/get-all")
    public ApiRes<?> findAll() {
        return apiResUtil.returnApiRes(null, null, null, lessonService.findAll());
    }

    @GetMapping("/lesson/get-by-chapter")
    public ApiRes<?> findById(@RequestParam Long chapterId) {
        return apiResUtil.returnApiRes(null, null, null, lessonService.findAllByChapterId(chapterId));
    }
}
