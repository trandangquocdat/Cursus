package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.CheckAnswerReq;
import com.fpt.cursus.dto.response.QuizRes;
import com.fpt.cursus.dto.response.QuizResultRes;
import com.fpt.cursus.entity.Quiz;
import com.fpt.cursus.service.QuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class QuizControllerTest {

    @Mock
    private QuizService quizService;

    @InjectMocks
    private QuizController quizController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private void setUpSecurityContext() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("user", "password", "ADMIN");
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void testGetQuizById_Success() {
        Long quizId = 1L;
        QuizRes quizRes = new QuizRes();
        when(quizService.getQuizById(quizId)).thenReturn(quizRes);

        ResponseEntity<Object> response = quizController.getQuizById(quizId);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(quizRes, response.getBody());
        verify(quizService, times(1)).getQuizById(quizId);
    }

    @Test
    void testGetQuizById_Failure() {
        Long quizId = 1L;
        when(quizService.getQuizById(quizId)).thenThrow(new RuntimeException("Quiz not found"));

        Exception exception = null;
        try {
            quizController.getQuizById(quizId);
        } catch (Exception e) {
            exception = e;
        }

        assertNotNull(exception);
        assertEquals("Quiz not found", exception.getMessage());
        verify(quizService, times(1)).getQuizById(quizId);
    }

    @Test
    void testGetAnswerById_Success() {
        setUpSecurityContext();
        Long answerId = 1L;
        QuizRes quizRes = new QuizRes();
        when(quizService.getAnswerById(answerId)).thenReturn(quizRes);

        ResponseEntity<Object> response = quizController.getAnswerById(answerId);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(quizRes, response.getBody());
        verify(quizService, times(1)).getAnswerById(answerId);
    }

    @Test
    void testGetAnswerById_Failure() {
        setUpSecurityContext();
        Long answerId = 1L;
        when(quizService.getAnswerById(answerId)).thenThrow(new RuntimeException("Answer not found"));

        Exception exception = null;
        try {
            quizController.getAnswerById(answerId);
        } catch (Exception e) {
            exception = e;
        }

        assertNotNull(exception);
        assertEquals("Answer not found", exception.getMessage());
        verify(quizService, times(1)).getAnswerById(answerId);
    }

    @Test
    void testCreateQuiz_Success() {
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx", MediaType.MULTIPART_FORM_DATA_VALUE, "Test content".getBytes());
        Long courseId = 1L;
        String name = "Test Quiz";
        Quiz quiz = new Quiz();
        when(quizService.createQuiz(any(MultipartFile.class), eq(courseId), eq(name))).thenReturn(quiz);

        ResponseEntity<Object> response = quizController.createQuiz(file, courseId, name);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(quiz, response.getBody());
        verify(quizService, times(1)).createQuiz(any(MultipartFile.class), eq(courseId), eq(name));
    }

    @Test
    void testCreateQuiz_Failure() {
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx", MediaType.MULTIPART_FORM_DATA_VALUE, "Test content".getBytes());
        Long courseId = 1L;
        String name = "Test Quiz";
        when(quizService.createQuiz(any(MultipartFile.class), eq(courseId), eq(name))).thenThrow(new RuntimeException("Quiz creation failed"));

        Exception exception = null;
        try {
            quizController.createQuiz(file, courseId, name);
        } catch (Exception e) {
            exception = e;
        }

        assertNotNull(exception);
        assertEquals("Quiz creation failed", exception.getMessage());
        verify(quizService, times(1)).createQuiz(any(MultipartFile.class), eq(courseId), eq(name));
    }

    @Test
    void testScoringQuiz_Success() {
        CheckAnswerReq request = new CheckAnswerReq();
        QuizResultRes result = new QuizResultRes();
        when(quizService.scoringQuiz(request)).thenReturn(result);

        ResponseEntity<Object> response = quizController.scoringQuiz(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(result, response.getBody());
        verify(quizService, times(1)).scoringQuiz(request);
    }

    @Test
    void testScoringQuiz_Failure() {
        CheckAnswerReq request = new CheckAnswerReq();
        when(quizService.scoringQuiz(request)).thenThrow(new RuntimeException("Scoring failed"));

        Exception exception = null;
        try {
            quizController.scoringQuiz(request);
        } catch (Exception e) {
            exception = e;
        }

        assertNotNull(exception);
        assertEquals("Scoring failed", exception.getMessage());
        verify(quizService, times(1)).scoringQuiz(request);
    }
}
