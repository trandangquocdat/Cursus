package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.CreateFeedbackDto;
import com.fpt.cursus.enums.FeedbackType;
import com.fpt.cursus.util.ApiResUtil;
import com.fpt.cursus.service.FeedbackService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@Tag(name = "Feedback Controller")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/feedback/create")
    public ResponseEntity<Object> createFeedback(@RequestParam Long courseId,
                                                 @RequestParam FeedbackType type,
                                                 @RequestBody CreateFeedbackDto feedbackDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(feedbackService.createFeedback(courseId, type, feedbackDto));
    }

    @DeleteMapping("/feedback/delete")
    public ResponseEntity<Object> deleteFeedback(@RequestParam Long id) {
        feedbackService.deleteFeedbackById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Delete feedback successfully");
    }

    @PutMapping("/feedback/update")
    public ResponseEntity<Object> updateFeedback(@RequestParam Long id,
                                                 @RequestBody CreateFeedbackDto feedbackDto) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(feedbackService.updateFeedbackById(id, feedbackDto));
    }

    @GetMapping("/feedback/view-by-course-id")
    public ResponseEntity<Object> getFeedbackByCourseId(@RequestParam Long courseId,
                                                        @RequestParam FeedbackType type) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(feedbackService.getFeedbackByCourseIdAndType(courseId, type));
    }

    @GetMapping("/feedback/view-by-type")
    public ResponseEntity<Object> viewFeedbackByType(@RequestParam FeedbackType type) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(feedbackService.getFeedbackByType(type));
    }

}
