package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.CreateChapterRequest;
import com.fpt.cursus.dto.request.CreateLessonDto;
import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.service.LessonService;
import com.fpt.cursus.util.ApiResUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class LessonController {
    @Autowired
    private ApiResUtil apiResUtil;
    @Autowired
    private LessonService lessonService;

    @PostMapping("/lesson")
    public ApiRes<?> createChapter(@RequestBody @Valid CreateLessonDto request) {
        String successMessage = "Create chapter successfully!";
        return apiResUtil.returnApiRes(true, HttpStatus.CREATED.value(), successMessage,
                lessonService.createLesson(request));
    }

    @PutMapping("/lesson/{lessonId}")
    public ApiRes<?> updateChapter(@PathVariable Long lessonId, @RequestBody @Valid CreateLessonDto request) {
        String successMessage = "Update chapter successfully!";
        return apiResUtil.returnApiRes(true, HttpStatus.OK.value(), successMessage,lessonService.updateLesson(lessonId, request));
    }

    @DeleteMapping("/lesson/{lessonId}")
    public ApiRes<?> deleteChapter(@PathVariable Long lessonId) {
        lessonService.deleteLessonById(lessonId);
        String successMessage = "Delete chapter successfully!";
        return apiResUtil.returnApiRes(true, HttpStatus.OK.value(), successMessage,null);
    }
}
