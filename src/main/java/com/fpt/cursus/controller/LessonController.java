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

    @PostMapping("/lesson/create")
    public ApiRes<?> createChapter(@RequestParam Long chapterId,@RequestBody @Valid CreateLessonDto request) {
        return apiResUtil.returnApiRes(null, null, null,
                lessonService.createLesson(chapterId,request));
    }

    @PutMapping("/lesson/update")
    public ApiRes<?> updateChapter(@RequestParam Long lessonId, @RequestBody @Valid CreateLessonDto request) {
        lessonService.updateLesson(lessonId, request);
        String successMessage = "Update chapter successfully!";
        return apiResUtil.returnApiRes(null, null, successMessage,null);
    }

    @DeleteMapping("/lesson/delete")
    public ApiRes<?> deleteChapter(@RequestParam Long lessonId) {
        lessonService.deleteLessonById(lessonId);
        String successMessage = "Delete chapter successfully!";
        return apiResUtil.returnApiRes(null, null, successMessage,null);
    }

    @GetMapping("/lesson/get-all")
    public ApiRes<?> findAll() {
        return apiResUtil.returnApiRes(null, null, null,lessonService.findAll());
    }
    @GetMapping("/lesson/get-by-chapter")
    public ApiRes<?> findById(@RequestParam  Long chapterId) {
        return apiResUtil.returnApiRes(null, null, null,lessonService.findAllByChapterId(chapterId));
    }
}
