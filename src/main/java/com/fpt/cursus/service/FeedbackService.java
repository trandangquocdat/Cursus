package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.CreateFeedbackDto;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.entity.Feedback;
import com.fpt.cursus.enums.type.FeedbackType;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.CourseRepo;
import com.fpt.cursus.repository.FeedbackRepo;
import com.fpt.cursus.util.AccountUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class FeedbackService {
    @Autowired
    private FeedbackRepo feedbackRepo;
    @Autowired
    private AccountUtil accountUtil;
    @Autowired
    private CourseService courseService;

//    @Resource
//    private ratingService
    public Feedback createFeedback(Long courseId,FeedbackType type,CreateFeedbackDto feedbackDto) {
        Feedback feedback = new Feedback();
        feedback.setContent(feedbackDto.getContent());
        feedback.setRating(feedbackDto.getRating());
        feedback.setCreatedDate(new Date());
        feedback.setCreatedBy(accountUtil.getCurrentAccount().getUsername());
        feedback.setCourse(courseService.getCourseById(courseId));
        feedback.setType(type);
        if(type == FeedbackType.REVIEW) {
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
        if(feedbacks == null){
            throw new AppException(ErrorCode.FEEDBACK_NOT_FOUND);
        }
        return feedbacks;
    }
    public List<Feedback> getFeedbackByType(FeedbackType type) {
        List<Feedback> feedbacks = feedbackRepo.findFeedbackByType(type);
        if(feedbacks == null){
            throw new AppException(ErrorCode.FEEDBACK_NOT_FOUND);
        }
        return feedbacks;
    }
    public List<Feedback> getFeedbackByCourseIdAndType(long id, FeedbackType type) {
        List<Feedback> feedbacks = feedbackRepo.findFeedbackByCourseIdAndType(id, type);
        if(feedbacks == null){
            throw new AppException(ErrorCode.FEEDBACK_NOT_FOUND);
        }
        return feedbacks;
    }


}
