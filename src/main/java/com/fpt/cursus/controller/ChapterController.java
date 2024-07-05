package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.CreateChapterRequest;
import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.service.ChapterService;
import com.fpt.cursus.util.ApiResUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@Tag(name = "Chapter Controller")
public class ChapterController {
    private final ChapterService chapterService;
    private final ApiResUtil apiResUtil;

    public ChapterController(ChapterService chapterService, ApiResUtil apiResUtil) {
        this.chapterService = chapterService;
        this.apiResUtil = apiResUtil;
    }

    @PostMapping("/chapter/create")
    public ApiRes<Object> createChapter(@RequestParam Long courseId,@RequestBody @Valid CreateChapterRequest request) {
        return apiResUtil.returnApiRes(null, null, null,
                chapterService.createChapter(courseId,request));
    }
    @DeleteMapping("/chapter/delete")
    public ApiRes<Object> deleteChapter(@RequestParam Long chapterId) {
        chapterService.deleteChapterById(chapterId);
        String successMessage = "Delete chapter successfully";
        return apiResUtil.returnApiRes(null, null, successMessage,null);
    }
    @PutMapping("/chapter/update/{chapterId}")
    public ApiRes<Object> updateChapter(@PathVariable Long chapterId, @RequestBody @Valid CreateChapterRequest request) {
        chapterService.updateChapter(chapterId, request);
        String successMessage = "Update chapter successfully";
        return apiResUtil.returnApiRes(null, null, successMessage,null);
    }

    @GetMapping("/chapter/get-by-id/{chapterId}")
    public ApiRes<Object> findChapterById(@PathVariable Long chapterId) {
        return apiResUtil.returnApiRes(null, null, null,chapterService.findChapterById(chapterId));
    }

    @GetMapping("/chapter/get-all")
    public ApiRes<Object> findAll() {
        return apiResUtil.returnApiRes(null, null, null,chapterService.findAll());
    }

    @GetMapping("/chapter/get-by-course")
    public ApiRes<Object> findAllByCourseId(@RequestParam Long courseId) {
        return apiResUtil.returnApiRes(null, null, null,chapterService.findAllByCourseId(courseId));
    }

}
