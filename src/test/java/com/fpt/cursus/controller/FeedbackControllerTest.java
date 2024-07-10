package com.fpt.cursus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.dto.request.CreateFeedbackDto;
import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.entity.Feedback;
import com.fpt.cursus.enums.FeedbackType;
import com.fpt.cursus.service.FeedbackService;
import com.fpt.cursus.util.ApiResUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(SpringExtension.class)
@WebMvcTest(FeedbackController.class)
@ContextConfiguration(classes = {
        FeedbackService.class,
        ApiResUtil.class
})
class FeedbackControllerTest {

    @MockBean
    private FeedbackService feedbackService;

    @MockBean
    private ApiResUtil apiResUtil;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new FeedbackController(feedbackService))
                .alwaysDo(print())
                .build();
    }

    // Tests for createFeedback endpoint
    @Test
    void testCreateFeedback_Success() throws Exception {
        CreateFeedbackDto requestDto = new CreateFeedbackDto();
        requestDto.setContent("Great course!");
        requestDto.setRating(4.5f);

        String json = objectMapper.writeValueAsString(requestDto);

        Feedback feedback = new Feedback();
        feedback.setId(1L);
        feedback.setContent("Great course feedback");

        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setMessage("Create feedback successfully");
        apiRes.setData(feedback);

        when(feedbackService.createFeedback(anyLong(), any(FeedbackType.class), any(CreateFeedbackDto.class)))
                .thenReturn(feedback);
        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
                .thenReturn(apiRes);

        mockMvc.perform(post("/feedback/")
                        .param("courseId", "1")
                        .param("type", FeedbackType.REVIEW.name())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpectAll(status().isOk(),
                        jsonPath("$.message").value("Create feedback successfully"),
                        jsonPath("$.data.id").value(1L),
                        jsonPath("$.data.content").value("Great course feedback"));
    }

    // Test for deleteFeedback endpoint
    @Test
    void testDeleteFeedback_Success() throws Exception {
        String successMessage = "Delete feedback successfully";
        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setMessage(successMessage);

        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
                .thenReturn(apiRes);
        doNothing().when(feedbackService).deleteFeedbackById(anyLong());

        mockMvc.perform(delete("/feedback/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.message").value(successMessage));
    }

    // Test for updateFeedback endpoint
    @Test
    void testUpdateFeedback_Success() throws Exception {
        CreateFeedbackDto requestDto = new CreateFeedbackDto();
        requestDto.setContent("Updated feedback content");
        requestDto.setRating(4.0f);

        String json = objectMapper.writeValueAsString(requestDto);

        String successMessage = "Update feedback successfully";
        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setMessage(successMessage);

        doNothing().when(feedbackService).updateFeedbackById(anyLong(), any(CreateFeedbackDto.class));
        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
                .thenReturn(apiRes);

        mockMvc.perform(put("/feedback/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpectAll(status().isOk(),
                        jsonPath("$.message").value(successMessage));
    }

    // Test for getFeedbackByCourseId endpoint
    @Test
    void testGetFeedbackByCourseId_Success() throws Exception {
        Feedback feedback = new Feedback();
        feedback.setId(1L);
        feedback.setContent("Course feedback");

        List<Feedback> feedbackList = Collections.singletonList(feedback);

        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setData(feedbackList);

        when(feedbackService.getFeedbackByCourseId(anyLong()))
                .thenReturn(feedbackList);
        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
                .thenReturn(apiRes);

        mockMvc.perform(get("/feedback/{courseId}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.data[0].id").value(feedback.getId()),
                        jsonPath("$.data[0].content").value(feedback.getContent()));
    }

    // Test for getReviewFeedback endpoint
    @Test
    void testGetReviewFeedback_Success() throws Exception {
        Feedback feedback = new Feedback();
        feedback.setId(1L);
        feedback.setContent("Review feedback");

        List<Feedback> reviewFeedbackList = Collections.singletonList(feedback);

        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setData(reviewFeedbackList);

        when(feedbackService.getFeedbackByType(FeedbackType.REVIEW))
                .thenReturn(reviewFeedbackList);
        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
                .thenReturn(apiRes);

        mockMvc.perform(get("/feedback/review")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.data[0].id").value(feedback.getId()),
                        jsonPath("$.data[0].content").value(feedback.getContent()));
    }

    // Test for getReportFeedback endpoint
    @Test
    void testGetReportFeedback_Success() throws Exception {
        Feedback feedback = new Feedback();
        feedback.setId(1L);
        feedback.setContent("Report feedback");

        List<Feedback> reportFeedbackList = Collections.singletonList(feedback);

        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setData(reportFeedbackList);

        when(feedbackService.getFeedbackByType(FeedbackType.REPORT))
                .thenReturn(reportFeedbackList);
        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
                .thenReturn(apiRes);

        mockMvc.perform(get("/feedback/report")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.data[0].id").value(feedback.getId()),
                        jsonPath("$.data[0].content").value(feedback.getContent()));
    }

    // Test for getFeedbackByCourseIdAndType endpoint
    @Test
    void testGetFeedbackByCourseIdAndType_Success() throws Exception {
        Feedback feedback = new Feedback();
        feedback.setId(1L);
        feedback.setContent("Course and type feedback");

        List<Feedback> feedbackList = Collections.singletonList(feedback);

        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setData(feedbackList);

        when(feedbackService.getFeedbackByCourseIdAndType(anyLong(), any(FeedbackType.class)))
                .thenReturn(feedbackList);
        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
                .thenReturn(apiRes);

        mockMvc.perform(get("/feedback")
                        .param("courseId", "1")
                        .param("type", FeedbackType.REVIEW.name())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.data[0].id").value(feedback.getId()),
                        jsonPath("$.data[0].content").value(feedback.getContent()));
    }
}
