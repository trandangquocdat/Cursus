package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.CreateChapterRequest;
import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.service.ChapterService;
import com.fpt.cursus.service.impl.ChapterServiceImpl;
import com.fpt.cursus.util.ApiResUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class ChapterController {
    @Autowired
    private ChapterService chapterService;
    @Autowired
    private ApiResUtil apiResUtil;

    @PostMapping("/chapter/create")
    public ApiRes<?> createChapter(@RequestParam Long courseId, @RequestBody @Valid CreateChapterRequest request) {
        String successMessage = "Create chapter successfully";
        return apiResUtil.returnApiRes(null, null, successMessage,
                chapterService.createChapter(courseId, request));
    }

    @DeleteMapping("/chapter/delete/{chapterId}")
    public ApiRes<?> deleteChapter(@PathVariable Long chapterId) {
        chapterService.deleteChapterById(chapterId);
        String successMessage = "Delete chapter successfully";
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }

    @PutMapping("/chapter/update/{chapterId}")
    public ApiRes<?> updateChapter(@PathVariable Long chapterId, @RequestBody @Valid CreateChapterRequest request) {
        chapterService.updateChapter(chapterId, request);
        String successMessage = "Update chapter successfully";
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }

    @GetMapping("/chapter/get-by-id/{chapterId}")
    public ApiRes<?> findChapterById(@PathVariable Long chapterId) {
        return apiResUtil.returnApiRes(null, null, null, chapterService.findChapterById(chapterId));
    }

    @GetMapping("/chapter/get-all")
    public ApiRes<?> findAll() {
        return apiResUtil.returnApiRes(null, null, null, chapterService.findAll());
    }

    @GetMapping("/chapter/get-by-course")
    public ApiRes<?> findAllByCourseId(@RequestParam Long courseId) {
        return apiResUtil.returnApiRes(null, null, null, chapterService.findAllByCourseId(courseId));
    }

}
