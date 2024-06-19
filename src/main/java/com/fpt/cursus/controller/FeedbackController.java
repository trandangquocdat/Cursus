package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.CreateFeedbackDto;
import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.enums.type.FeedbackType;
import com.fpt.cursus.util.ApiResUtil;
import com.fpt.cursus.service.FeedbackService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class FeedbackController {
    @Autowired
    private ApiResUtil apiResUtil;
    @Autowired
    private FeedbackService feedbackService;

    @PostMapping("/feedback/{courseId}")
    public ApiRes<?> createFeedback(@PathVariable Long courseId, @RequestBody CreateFeedbackDto feedbackDto) {
        String successMessage = "Create feedback successfully";

        return apiResUtil.returnApiRes(true, HttpStatus.CREATED.value(), successMessage,
                feedbackService.createFeedback(courseId, feedbackDto));
    }

    @DeleteMapping("/feedback/{id}")
    public ApiRes<?> deleteFeedback(@PathVariable Long id) {
        String successMessage = "Delete feedback successfully";
        feedbackService.deleteFeedbackById(id);
        return apiResUtil.returnApiRes(true, HttpStatus.OK.value(), successMessage, null);
    }

    @PutMapping("/feedback/{id}")
    public ApiRes<?> updateFeedback(@PathVariable Long id, @RequestBody CreateFeedbackDto feedbackDto) {
        String successMessage = "Update feedback successfully";
        return apiResUtil.returnApiRes(true, HttpStatus.OK.value(), successMessage,
                feedbackService.updateFeedbackById(id, feedbackDto));
    }

    @GetMapping("/feedback/{courseId}")
    public ApiRes<?> getFeedbackByCourseId(@PathVariable Long courseId) {
        String successMessage = "Get feedback successfully";
        return apiResUtil.returnApiRes(true, HttpStatus.OK.value(), successMessage,
                feedbackService.getFeedbackByCourseId(courseId));
    }
    @GetMapping("/feedback/review")
    public ApiRes<?> getReviewFeedback() {
        String successMessage = "Get review successfully";
        return apiResUtil.returnApiRes(true, HttpStatus.OK.value(), successMessage,
                feedbackService.getFeedbackByType(FeedbackType.REVIEW));
    }
    @GetMapping("/feedback/report")
    public ApiRes<?> getReportFeedback() {
        String successMessage = "Get report successfully";
        return apiResUtil.returnApiRes(true, HttpStatus.OK.value(), successMessage,
                feedbackService.getFeedbackByType(FeedbackType.REPORT));
    }

}
