package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.CreateFeedbackDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.entity.Feedback;
import com.fpt.cursus.enums.FeedbackType;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.repository.FeedbackRepo;
import com.fpt.cursus.service.impl.FeedbackServiceImpl;
import com.fpt.cursus.util.AccountUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {
    @InjectMocks
    private FeedbackServiceImpl feedbackService;

    @Mock
    private FeedbackRepo feedbackRepo;

    @Mock
    private AccountUtil accountUtil;

    @Mock
    private CourseService courseService;

    @BeforeEach
    void setUp() {

    }

    @Test
    void testCreateFeedbackWithNonReviewType() {
        //given
        long courseId = 1L;
        FeedbackType type = FeedbackType.REPORT;
        CreateFeedbackDto feedbackDto = new CreateFeedbackDto();
        feedbackDto.setContent("Test content");
        feedbackDto.setRating(4.5F);

        Account mockAccount = new Account();
        mockAccount.setUsername("testUser");

        Course mockCourse = new Course();
        mockCourse.setId(courseId);

        //when
        when(accountUtil.getCurrentAccount()).thenReturn(mockAccount);
        when(courseService.getCourseById(anyLong())).thenReturn(mockCourse);
        when(feedbackRepo.save(any(Feedback.class))).thenAnswer(invocation -> {
            Feedback savedFeedback = invocation.getArgument(0);
            savedFeedback.setId(1L); // mock saved ID
            return savedFeedback;
        });

        Feedback feedback = feedbackService.createFeedback(courseId, type, feedbackDto);

        //then
        assertNotNull(feedback);
        assertEquals(feedbackDto.getContent(), feedback.getContent());
        assertEquals(feedbackDto.getRating(), feedback.getRating());
        assertEquals(FeedbackType.REPORT, feedback.getType());
        assertNotNull(feedback.getCreatedDate());
        assertEquals("testUser", feedback.getCreatedBy());
    }

    @Test
    void testCalculateAverageRatingWithMultipleFeedbacks() {
        //given
        long courseId = 1L;

        Course mockCourse = new Course();
        mockCourse.setId(courseId);
        mockCourse.setRating(3.0f); // initial rating

        List<Feedback> mockFeedbacks = Arrays.asList(
                new Feedback(1L, "Feedback 1", 4.0f, new Date(System.currentTimeMillis()), "user1", FeedbackType.REVIEW, mockCourse),
                new Feedback(2L, "Feedback 2", 3.0f, new Date(System.currentTimeMillis()), "user2", FeedbackType.REVIEW, mockCourse),
                new Feedback(3L, "Feedback 3", 2.0f, new Date(System.currentTimeMillis()), "user3", FeedbackType.REVIEW, mockCourse)
        );

        float newRating = 5.0f;

        //when
        when(feedbackRepo.findFeedbackByCourseId(anyLong())).thenReturn(mockFeedbacks);
        when(courseService.getCourseById(anyLong())).thenReturn(mockCourse);


        feedbackService.ratingCourse(courseId, newRating);

        // Calculate expected average rating
        float expectedAverageRating = (float) (Math.round((4.0f + 3.0f + 2.0f + newRating) / (mockFeedbacks.size() + 1) * 10.0) / 10.0);

        //then
        assertEquals(expectedAverageRating, mockCourse.getRating());
        verify(courseService, times(1)).saveCourse(mockCourse);
    }

    @Test
    void testCreateFeedback() {
        //given
        CreateFeedbackDto feedbackDto = new CreateFeedbackDto();
        feedbackDto.setContent("Test feedback");
        feedbackDto.setRating(4.5f);
        Account mockAccount = new Account();
        mockAccount.setUsername("testUser");
        Course mockCourse = new Course();
        mockCourse.setId(1L);

        //when
        when(accountUtil.getCurrentAccount()).thenReturn(mockAccount);
        when(courseService.getCourseById(anyLong())).thenReturn(mockCourse);
        when(feedbackRepo.save(any(Feedback.class))).thenAnswer(invocation -> {
            Feedback savedFeedback = invocation.getArgument(0);
            savedFeedback.setId(1L); // mock saved ID
            return savedFeedback;
        });

        Feedback feedback = feedbackService.createFeedback(1L, FeedbackType.REVIEW, feedbackDto);

        //then
        assertNotNull(feedback);
        assertEquals(feedbackDto.getContent(), feedback.getContent());
        assertEquals(feedbackDto.getRating(), feedback.getRating());
        assertEquals(FeedbackType.REVIEW, feedback.getType());
        assertNotNull(feedback.getCreatedDate());
        assertEquals("testUser", feedback.getCreatedBy());

        verify(feedbackRepo, times(1)).save(any(Feedback.class));
    }

    @Test
    void testCreateFeedbackWithInvalidRating() {
        //given
        Long courseId = 1L;
        FeedbackType type = FeedbackType.REVIEW;
        CreateFeedbackDto feedbackDto = new CreateFeedbackDto();
        feedbackDto.setContent("Test content");
        feedbackDto.setRating(6.5F); // Assuming an invalid rating

        //when
        Account mockAccount = new Account();
        mockAccount.setUsername("testuser");

        when(accountUtil.getCurrentAccount()).thenReturn(mockAccount);

        Course mockCourse = new Course();
        mockCourse.setId(courseId);
        when(courseService.getCourseById(courseId)).thenReturn(mockCourse);

        assertThrows(AppException.class, () -> feedbackService.createFeedback(courseId, type, feedbackDto));

        // Verify method interactions
        verify(accountUtil, times(1)).getCurrentAccount();
        verify(courseService, times(1)).getCourseById(courseId);
        verify(feedbackRepo, never()).save(any(Feedback.class)); // Ensure save was not called
    }

    @Test
    void testRatingCourse() {
        //given
        long courseId = 1L;
        float newRating = 4.5f;

        Course mockCourse = new Course();
        mockCourse.setId(courseId);
        mockCourse.setRating(4.0f);
        List<Feedback> mockFeedbacks = List.of
                (new Feedback(1L, "Good course", 4.0f, new Date(System.currentTimeMillis()), "user1", FeedbackType.REVIEW, mockCourse),
                        (new Feedback(2L, "Bad course", 2.0f, new Date(System.currentTimeMillis()), "user2", FeedbackType.REVIEW, mockCourse)));

        //when
        when(feedbackRepo.findFeedbackByCourseId(anyLong())).thenReturn(mockFeedbacks);
        when(courseService.getCourseById(anyLong())).thenReturn(mockCourse);

        feedbackService.ratingCourse(courseId, newRating);

        //then
        assertEquals(3.5f, mockCourse.getRating());
        verify(courseService, times(1)).saveCourse(any(Course.class));
    }

    @Test
    void testDeleteFeedbackById() {
        //given
        long feedbackId = 1L;

        //when
        doNothing().when(feedbackRepo).deleteById(feedbackId);
        feedbackService.deleteFeedbackById(feedbackId);

        //then
        verify(feedbackRepo, times(1)).deleteById(feedbackId);
    }

    @Test
    void testUpdateFeedbackById() {
        //given
        long feedbackId = 1L;
        CreateFeedbackDto feedbackDto = new CreateFeedbackDto();
        feedbackDto.setContent("Updated content");

        Feedback mockFeedback = new Feedback();
        mockFeedback.setId(feedbackId);
        mockFeedback.setContent("Original content");

        //when
        when(feedbackRepo.findFeedbackById(feedbackId)).thenReturn(mockFeedback);
        when(feedbackRepo.save(any(Feedback.class))).thenAnswer(invocation -> {
            Feedback updatedFeedback = invocation.getArgument(0);
            updatedFeedback.setUpdatedDate(new Date(System.currentTimeMillis()));
            return updatedFeedback;
        });

        feedbackService.updateFeedbackById(feedbackId, feedbackDto);

        //then
        verify(feedbackRepo, times(1)).findFeedbackById(feedbackId);
        verify(feedbackRepo, times(1)).save(any(Feedback.class));
    }

    @Test
    void testGetFeedbackByType() {
        //given
        FeedbackType type = FeedbackType.REVIEW;
        List<Feedback> mockFeedbackList = Arrays.asList(new Feedback(), new Feedback());

        //when
        when(feedbackRepo.findFeedbackByType(type)).thenReturn(mockFeedbackList);

        List<Feedback> feedbacks = feedbackService.getFeedbackByType(type);

        //then
        assertEquals(mockFeedbackList.size(), feedbacks.size());
    }

    @Test
    void testGetFeedbackByCourseIdAndType() {
        //given
        long courseId = 1L;
        FeedbackType type = FeedbackType.REVIEW;
        List<Feedback> mockFeedbackList = Arrays.asList(new Feedback(), new Feedback());

        //when
        when(feedbackRepo.findFeedbackByCourseId(courseId)).thenReturn(mockFeedbackList);
        when(feedbackRepo.findFeedbackByCourseIdAndType(courseId, type)).thenReturn(mockFeedbackList);

        List<Feedback> feedbacks1 = feedbackService.getFeedbackByCourseIdAndType(courseId, null);
        List<Feedback> feedbacks2 = feedbackService.getFeedbackByCourseIdAndType(courseId, type);

        //then
        assertEquals(mockFeedbackList.size(), feedbacks1.size());
        assertEquals(mockFeedbackList.size(), feedbacks2.size());
    }

    @Test
    void testGetFeedbackByCourseIdAndTypeWhenNotFound() {
        //given
        long courseId = 1L;
        FeedbackType type = FeedbackType.REVIEW;

        //when
        when(feedbackRepo.findFeedbackByCourseIdAndType(courseId, type)).thenReturn(null);

        //then
        assertThrows(AppException.class, () -> feedbackService.getFeedbackByCourseIdAndType(courseId, type));
        verify(feedbackRepo, times(1)).findFeedbackByCourseIdAndType(courseId, type);
    }

    @Test
    void testGetFeedbackByCourseIdAndTypeWhenNullButNoException() {
        //given
        long courseId = 1L;
        FeedbackType type = FeedbackType.REVIEW;

        //when
        when(feedbackRepo.findFeedbackByCourseIdAndType(courseId, type)).thenReturn(Collections.emptyList());


        List<Feedback> feedbacks = feedbackService.getFeedbackByCourseIdAndType(courseId, type);

        //then
        verify(feedbackRepo, times(1)).findFeedbackByCourseIdAndType(courseId, type);
        assertNotNull(feedbacks);
        assertTrue(feedbacks.isEmpty());
    }
}
