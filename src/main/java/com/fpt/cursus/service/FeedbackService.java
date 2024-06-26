package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.CreateFeedbackDto;
import com.fpt.cursus.entity.Feedback;

public interface FeedbackService {
    Feedback createFeedback(Long courseId, CreateFeedbackDto feedbackDto);

    void deleteFeedbackById(long id);


}
