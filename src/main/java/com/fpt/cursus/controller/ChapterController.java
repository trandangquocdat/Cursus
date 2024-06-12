package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.CreateChapterRequest;
import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.service.ChapterService;
import com.fpt.cursus.util.ApiResUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class ChapterController {
    @Autowired
    private ChapterService chapterService;
    @Autowired
    private ApiResUtil apiResUtil;

    @PostMapping("/chapter")
    public ApiRes<?> createChapter(@RequestBody @Valid CreateChapterRequest request) {
        String successMessage = "Create chapter successfully";
        return apiResUtil.returnApiRes(true, HttpStatus.CREATED.value(), successMessage,
                chapterService.createChapter(request));
    }
    @DeleteMapping("/chapter/{chapterId}")
    public ApiRes<?> deleteChapter(@PathVariable Long chapterId) {
        chapterService.deleteChapterById(chapterId);
        String successMessage = "Delete chapter successfully";
        return apiResUtil.returnApiRes(true, HttpStatus.OK.value(), successMessage,null);
    }
    @PutMapping("/chapter/{chapterId}")
    public ApiRes<?> updateChapter(@PathVariable Long chapterId, @RequestBody @Valid CreateChapterRequest request) {
        chapterService.updateChapter(chapterId, request);
        String successMessage = "Update chapter successfully";
        return apiResUtil.returnApiRes(true, HttpStatus.OK.value(), successMessage,null);
    }

}
