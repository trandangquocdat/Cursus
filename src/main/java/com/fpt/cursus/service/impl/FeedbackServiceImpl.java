package com.fpt.cursus.service.impl;

import com.fpt.cursus.dto.request.CreateFeedbackDto;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.entity.Feedback;
import com.fpt.cursus.enums.FeedbackType;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.FeedbackRepo;
import com.fpt.cursus.service.CourseService;
import com.fpt.cursus.service.FeedbackService;
import com.fpt.cursus.util.AccountUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {
    private static final List<Float> VALID_RATINGS = Arrays.asList(1.0f, 1.5f, 2.0f, 2.5f, 3.0f, 3.5f, 4.0f, 4.5f, 5.0f);
    private final FeedbackRepo feedbackRepo;
    private final AccountUtil accountUtil;
    private final CourseService courseService;

    @Autowired
    public FeedbackServiceImpl(FeedbackRepo feedbackRepo,
                               AccountUtil accountUtil,
                               CourseService courseService) {
        this.feedbackRepo = feedbackRepo;
        this.accountUtil = accountUtil;
        this.courseService = courseService;
    }

    //    @Resource
//    private ratingService
    public Feedback createFeedback(Long courseId, FeedbackType type, CreateFeedbackDto feedbackDto) {
        Feedback feedback = new Feedback();
        feedback.setContent(feedbackDto.getContent());
        feedback.setRating(feedbackDto.getRating());
        feedback.setCreatedDate(new Date());
        feedback.setCreatedBy(accountUtil.getCurrentAccount().getUsername());
        feedback.setCourse(courseService.getCourseById(courseId));
        feedback.setType(type);
        if (type == FeedbackType.REVIEW) {
            if (!VALID_RATINGS.contains(feedbackDto.getRating()))
                throw new AppException(ErrorCode.FEEDBACK_INVALID_RATING);
            ratingCourse(courseId, feedbackDto.getRating());
        }
        return feedbackRepo.save(feedback);
    }

    public void ratingCourse(long courseId, float rating) {
        List<Feedback> feedbacks = getFeedbackByCourseIdAndType(courseId, null);
        float sum = 0;
        for (Feedback feedback : feedbacks) {
            sum += feedback.getRating();
        }
        sum += rating;
        Course course = courseService.getCourseById(courseId);
        course.setRating((float) (Math.round(sum / (feedbacks.size() + 1) * 10.0) / 10.0));
        courseService.saveCourse(course);
    }

    public void deleteFeedbackById(long id) {
        feedbackRepo.deleteById(id);
    }

    public Feedback updateFeedbackById(long id, CreateFeedbackDto feedbackDto) {
        Feedback feedback = feedbackRepo.findFeedbackById(id);
        feedback.setContent(feedbackDto.getContent());
        feedback.setUpdatedDate(new Date());
        return feedbackRepo.save(feedback);
    }

    public List<Feedback> getFeedbackByType(FeedbackType type) {
        return feedbackRepo.findFeedbackByType(type);
    }

    public List<Feedback> getFeedbackByCourseIdAndType(long id, FeedbackType type) {
        List<Feedback> feedbacks;
        if (type == null) {
            feedbacks = feedbackRepo.findFeedbackByCourseId(id);
        } else {
            feedbacks = feedbackRepo.findFeedbackByCourseIdAndType(id, type);
        }
        if (feedbacks == null) {
            throw new AppException(ErrorCode.FEEDBACK_NOT_FOUND);
        }
        return feedbacks;
    }


}
