package com.fpt.cursus.service.impl;

import com.fpt.cursus.dto.request.CreateFeedbackDto;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.entity.Feedback;
import com.fpt.cursus.enums.type.FeedbackType;
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
    private final FeedbackRepo feedbackRepo;
    private final AccountUtil accountUtil;
    private final CourseService courseService;
    private static final List<Double> VALID_RATINGS = Arrays.asList(1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0);

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
        List<Feedback> feedbacks = getFeedbackByCourseId(courseId);
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
        return feedbackRepo.findFeedbackByType(type);
    }

    public List<Feedback> getFeedbackByCourseIdAndType(long id, FeedbackType type) {
        List<Feedback> feedbacks = feedbackRepo.findFeedbackByCourseIdAndType(id, type);
        if (feedbacks == null) {
            throw new AppException(ErrorCode.FEEDBACK_NOT_FOUND);
        }
        return feedbacks;
    }


}
