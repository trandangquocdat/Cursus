package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.CreateFeedbackDto;
import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.enums.type.FeedbackType;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.util.ApiResUtil;
import com.fpt.cursus.service.FeedbackService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class FeedbackController {
    private static final List<Double> VALID_RATINGS = Arrays.asList(1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0);
    @Autowired
    private ApiResUtil apiResUtil;
    @Autowired
    private FeedbackService feedbackService;

    @PostMapping("/feedback/")
    public ApiRes<?> createFeedback(@RequestParam Long courseId, @RequestParam FeedbackType type, @RequestBody CreateFeedbackDto feedbackDto) {
        double rating = feedbackDto.getRating();
        if (rating == 0 || !VALID_RATINGS.contains(rating)) {
            return apiResUtil.returnApiRes(false, ErrorCode.FEEDBACK_INVALID_RATING.getCode(), ErrorCode.FEEDBACK_INVALID_RATING.getMessage(), null);
        }
        return apiResUtil.returnApiRes(null, null, null,
                feedbackService.createFeedback(courseId, type, feedbackDto));
    }

    @DeleteMapping("/feedback/{id}")
    public ApiRes<?> deleteFeedback(@PathVariable Long id) {
        String successMessage = "Delete feedback successfully";
        feedbackService.deleteFeedbackById(id);
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }

    @PutMapping("/feedback/{id}")
    public ApiRes<?> updateFeedback(@PathVariable Long id, @RequestBody CreateFeedbackDto feedbackDto) {
        feedbackService.updateFeedbackById(id, feedbackDto);
        String successMessage = "Update feedback successfully";
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }

    @GetMapping("/feedback/{courseId}")
    public ApiRes<?> getFeedbackByCourseId(@PathVariable Long courseId) {
        return apiResUtil.returnApiRes(null, null, null,
                feedbackService.getFeedbackByCourseId(courseId));
    }

    @GetMapping("/feedback/review")
    public ApiRes<?> getReviewFeedback() {
        return apiResUtil.returnApiRes(null, null, null,
                feedbackService.getFeedbackByType(FeedbackType.REVIEW));
    }

    @GetMapping("/feedback/report")
    public ApiRes<?> getReportFeedback() {
        return apiResUtil.returnApiRes(null, null, null,
                feedbackService.getFeedbackByType(FeedbackType.REPORT));
    }

    @GetMapping("/feedback")
    public ApiRes<?> getFeedbackByCourseIdAndType(@RequestParam Long courseId, @RequestParam FeedbackType type) {
        return apiResUtil.returnApiRes(null, null, null,
                feedbackService.getFeedbackByCourseIdAndType(courseId, type));
    }

}
