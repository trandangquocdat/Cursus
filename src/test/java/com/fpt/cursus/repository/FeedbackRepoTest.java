package com.fpt.cursus.repository;

import com.fpt.cursus.entity.Course;
import com.fpt.cursus.entity.Feedback;
import com.fpt.cursus.enums.type.FeedbackType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
class FeedbackRepoTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    FeedbackRepo repo;

    private Long feedbackId;
    private Long courseId;

    @BeforeEach
    void setUp() {
        Course course = new Course();
        entityManager.persist(course);
        courseId = course.getId();

        Feedback feedback = new Feedback();
        feedback.setRating(5f);
        feedback.setContent("This is a test");
        feedback.setCreatedBy("username");
        feedback.setCreatedDate(new Date());
        feedback.setUpdatedDate(new Date());
        feedback.setType(FeedbackType.REPORT);
        feedback.setCourse(course);
        entityManager.persist(feedback);
        feedbackId = feedback.getId();
    }

    @Test
    void testFindFeedbackById() {
        Feedback result = repo.findFeedbackById(feedbackId);
        assertEquals(feedbackId, result.getId());
    }

    @Test
    void testFindFeedbackByIdNotFound() {
        assertNull(repo.findFeedbackById(feedbackId + 1));
    }

    @Test
    void testFindFeedbackByCourseId() {
        List<Feedback> result = repo.findFeedbackByCourseId(courseId);
        assertEquals(feedbackId, result.get(0).getId());
    }

    @Test
    void testFindFeedbackByCourseIdNotFound() {
        assertEquals(0, repo.findFeedbackByCourseId(courseId + 1).size());
    }

    @Test
    void testFindFeedbackByType() {
        List<Feedback> result = repo.findFeedbackByType(FeedbackType.REPORT);
        assertEquals(feedbackId, result.get(0).getId());
    }

    @Test
    void testFindFeedbackByTypeNotFound() {
        assertEquals(0, repo.findFeedbackByType(FeedbackType.REVIEW).size());
    }

    @Test
    void testFindFeedbackByCourseIdAndType() {
        List<Feedback> result = repo.findFeedbackByCourseIdAndType(courseId, FeedbackType.REPORT);
        assertEquals(feedbackId, result.get(0).getId());
    }

    @Test
    void testFindFeedbackByCourseIdAndTypeNotFound() {
        assertEquals(0, repo.findFeedbackByCourseIdAndType(courseId, FeedbackType.REVIEW).size());
        assertEquals(0, repo.findFeedbackByCourseIdAndType(courseId + 1, FeedbackType.REPORT).size());
    }
}
