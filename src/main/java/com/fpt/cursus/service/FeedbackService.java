package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.CreateFeedbackDto;
import com.fpt.cursus.entity.Feedback;
import com.fpt.cursus.enums.FeedbackType;

import java.util.List;

public interface FeedbackService {
    Feedback createFeedback(Long courseId, FeedbackType type, CreateFeedbackDto feedbackDto);

    void ratingCourse(long courseId, float rating);

    void deleteFeedbackById(long id);

    Feedback updateFeedbackById(long id, CreateFeedbackDto feedbackDto);

    List<Feedback> getFeedbackByType(FeedbackType type);

    List<Feedback> getFeedbackByCourseIdAndType(long id, FeedbackType type);
}
