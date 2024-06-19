package com.fpt.cursus.util;

import com.fpt.cursus.dto.request.CreateFeedbackDto;
import com.fpt.cursus.entity.Feedback;
import com.fpt.cursus.repository.CourseRepo;
import com.fpt.cursus.repository.FeedbackRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class FeedbackService {
    @Autowired
    private FeedbackRepo feedbackRepo;
    @Autowired
    private AccountUtil accountUtil;
    @Autowired
    private CourseRepo courseRepo;

    public Feedback createFeedback(Long courseId,CreateFeedbackDto feedbackDto) {
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
    public Feedback updateFeedbackById(long id, CreateFeedbackDto feedbackDto) {
        Feedback feedback = feedbackRepo.findFeedbackById(id);
        feedback.setContent(feedbackDto.getContent());
        feedback.setUpdatedDate(new Date());
        return feedbackRepo.save(feedback);
    }
    public List<Feedback> getFeedbackByCourseId(long id) {
        return feedbackRepo.findFeedbackByCourseId(id);
    }

    public Feedback getFeedbackById(long id) {
        return feedbackRepo.findFeedbackById(id);
    }

}
