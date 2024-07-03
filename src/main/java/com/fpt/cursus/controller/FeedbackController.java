package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.CreateFeedbackDto;
import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.enums.type.FeedbackType;
import com.fpt.cursus.util.ApiResUtil;
import com.fpt.cursus.service.FeedbackService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@Tag(name = "Feedback Controller")
public class FeedbackController {

    private final ApiResUtil apiResUtil;
    private final FeedbackService feedbackService;

    public FeedbackController(ApiResUtil apiResUtil, FeedbackService feedbackService) {
        this.apiResUtil = apiResUtil;
        this.feedbackService = feedbackService;
    }

    @PostMapping("/feedback/")
    public ApiRes<Object> createFeedback(@RequestParam Long courseId, @RequestParam FeedbackType type, @RequestBody CreateFeedbackDto feedbackDto) {
        return apiResUtil.returnApiRes(null, null, null,
                feedbackService.createFeedback(courseId, type, feedbackDto));
    }

    @DeleteMapping("/feedback/{id}")
    public ApiRes<Object> deleteFeedback(@PathVariable Long id) {
        String successMessage = "Delete feedback successfully";
        feedbackService.deleteFeedbackById(id);
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }

    @PutMapping("/feedback/{id}")
    public ApiRes<Object> updateFeedback(@PathVariable Long id, @RequestBody CreateFeedbackDto feedbackDto) {
        feedbackService.updateFeedbackById(id, feedbackDto);
        String successMessage = "Update feedback successfully";
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }

    @GetMapping("/feedback/{courseId}")
    public ApiRes<Object> getFeedbackByCourseId(@PathVariable Long courseId) {
        return apiResUtil.returnApiRes(null, null, null,
                feedbackService.getFeedbackByCourseId(courseId));
    }

    @GetMapping("/feedback/review")
    public ApiRes<Object> getReviewFeedback() {
        return apiResUtil.returnApiRes(null, null, null,
                feedbackService.getFeedbackByType(FeedbackType.REVIEW));
    }

    @GetMapping("/feedback/report")
    public ApiRes<Object> getReportFeedback() {
        return apiResUtil.returnApiRes(null, null, null,
                feedbackService.getFeedbackByType(FeedbackType.REPORT));
    }

    @GetMapping("/feedback")
    public ApiRes<Object> getFeedbackByCourseIdAndType(@RequestParam Long courseId, @RequestParam FeedbackType type) {
        return apiResUtil.returnApiRes(null, null, null,
                feedbackService.getFeedbackByCourseIdAndType(courseId, type));
    }

}
