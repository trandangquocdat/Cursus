package com.fpt.cursus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.dto.object.UserAnswerDto;
import com.fpt.cursus.dto.request.CheckAnswerReq;
import com.fpt.cursus.dto.response.QuizRes;
import com.fpt.cursus.dto.response.QuizResultRes;
import com.fpt.cursus.entity.Quiz;
import com.fpt.cursus.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(QuizController.class)
@ContextConfiguration(classes = {
        QuizService.class
})
class QuizControllerTest {

    @MockBean
    private QuizService quizService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private QuizResultRes mockQuizResultRes;
    private QuizRes mockQuizRes;
    private CheckAnswerReq checkAnswerReq;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new QuizController(quizService))
                .alwaysDo(print())
                .build();

        mockQuizRes = new QuizRes();
        Quiz mockQuiz = new Quiz();
        mockQuiz.setId(1L);
        mockQuiz.setName("TestQuiz");
        mockQuiz.setCreatedBy("TestUser");
        mockQuizRes.setQuiz(mockQuiz);

        mockQuizResultRes = new QuizResultRes();
        mockQuizResultRes.setScore(0.25);
        mockQuizResultRes.setWrong(0);
        mockQuizResultRes.setSkipped(0);
        mockQuizResultRes.setCorrect(1);

        UserAnswerDto userAnswerDto = new UserAnswerDto();
        userAnswerDto.setAnswerId("1");
        userAnswerDto.setQuestionId(1);
        List<UserAnswerDto> userAnswerDtoList = Arrays.asList(userAnswerDto);

        checkAnswerReq = new CheckAnswerReq();
        checkAnswerReq.setQuizId(1);
        checkAnswerReq.setAnswers(userAnswerDtoList);

    }


    @Test
    void getQuizById() throws Exception {
        when(quizService.getQuizById(anyLong())).thenReturn(mockQuizRes);

        mockMvc.perform(get("/quiz")
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quiz.id").value(1L))
                .andExpect(jsonPath("$.quiz.name").value("TestQuiz"))
                .andExpect(jsonPath("$.quiz.createdBy").value("TestUser"));
    }

    @Test
    void getAnswerById() throws Exception {
        when(quizService.getAnswerById(anyLong())).thenReturn(mockQuizRes);

        mockMvc.perform(get("/quiz/answer")
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quiz.id").value(1L))
                .andExpect(jsonPath("$.quiz.name").value("TestQuiz"))
                .andExpect(jsonPath("$.quiz.createdBy").value("TestUser"));
    }

    @Test
    void createQuiz() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Test Data".getBytes());
        when(quizService.createQuiz(any(MultipartFile.class), anyLong(), anyString())).thenReturn(mockQuizRes.getQuiz());

        mockMvc.perform(multipart("/quiz")
                        .file(file)
                        .param("courseId", "1")
                        .param("name", "TestQuiz")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("TestQuiz"))
                .andExpect(jsonPath("$.createdBy").value("TestUser"));
    }

    @Test
    void scoringQuiz() throws Exception {
        when(quizService.scoringQuiz(any(CheckAnswerReq.class))).thenReturn(mockQuizResultRes);

        mockMvc.perform(put("/quiz/scoring")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkAnswerReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(1))
                .andExpect(jsonPath("$.wrong").value(0))
                .andExpect(jsonPath("$.skipped").value(0))
                .andExpect(jsonPath("$.score").value(0.25));
    }
}