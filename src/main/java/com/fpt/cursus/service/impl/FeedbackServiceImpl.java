package com.fpt.cursus.service.impl;

import com.fpt.cursus.dto.request.CreateFeedbackDto;
import com.fpt.cursus.entity.Feedback;
import com.fpt.cursus.enums.type.FeedbackType;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.CourseRepo;
import com.fpt.cursus.repository.FeedbackRepo;
import com.fpt.cursus.service.CourseService;
import com.fpt.cursus.service.FeedbackService;
import com.fpt.cursus.util.AccountUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {
    @Autowired
    private FeedbackRepo feedbackRepo;
    @Autowired
    private AccountUtil accountUtil;
    @Autowired
    private CourseRepo courseRepo;
    @Autowired
    private CourseService courseService;

    public Feedback createFeedback(Long courseId, CreateFeedbackDto feedbackDto) {
        Feedback feedback = new Feedback();
        feedback.setContent(feedbackDto.getContent());
        feedback.setCreatedDate(new Date());
        feedback.setCreatedBy(accountUtil.getCurrentAccount().getUsername());
        feedback.setCourse(courseRepo.findCourseById(courseId));
        return feedbackRepo.save(feedback);
    }

    public void deleteFeedbackById(long id) {
        feedbackRepo.deleteById(id);
    }

    public void updateFeedbackById(long id, CreateFeedbackDto feedbackDto) {
        Feedback feedback = feedbackRepo.findFeedbackById(id);
        feedback.setContent(feedbackDto.getContent());
        feedback.setUpdatedDate(new Date());
        feedbackRepo.save(feedback);
    }

    public List<Feedback> getFeedbackByCourseId(long id) {
        List<Feedback> feedbacks = feedbackRepo.findFeedbackByCourseId(id);
        if (feedbacks == null) {
            throw new AppException(ErrorCode.FEEDBACK_NOT_FOUND);
        }
        return feedbacks;
    }

    public List<Feedback> getFeedbackByType(FeedbackType type) {
        List<Feedback> feedbacks = feedbackRepo.findFeedbackByType(type);
        if (feedbacks == null) {
            throw new AppException(ErrorCode.FEEDBACK_NOT_FOUND);
        }
        return feedbacks;
    }

    public List<Feedback> getFeedbackByCourseIdAndType(long id, FeedbackType type) {
        List<Feedback> feedbacks = feedbackRepo.findFeedbackByCourseIdAndType(id, type);
        if (feedbacks == null) {
            throw new AppException(ErrorCode.FEEDBACK_NOT_FOUND);
        }
        return feedbacks;
    }

}
