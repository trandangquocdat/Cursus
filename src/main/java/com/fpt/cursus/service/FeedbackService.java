package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.CreateFeedbackDto;
import com.fpt.cursus.entity.Feedback;
import com.fpt.cursus.enums.type.FeedbackType;

import java.util.List;

public interface FeedbackService {
    Feedback createFeedback(Long courseId, CreateFeedbackDto feedbackDto);

    void deleteFeedbackById(long id);

    void updateFeedbackById(long id, CreateFeedbackDto feedbackDto);

    List<Feedback> getFeedbackByCourseId(long id);

    List<Feedback> getFeedbackByType(FeedbackType type);

    List<Feedback> getFeedbackByCourseIdAndType(long id, FeedbackType type);
}
