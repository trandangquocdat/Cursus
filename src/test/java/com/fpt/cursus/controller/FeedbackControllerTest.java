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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
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
        // Setup input DTO
        CreateFeedbackDto requestDto = new CreateFeedbackDto();
        requestDto.setContent("Great course!");
        requestDto.setRating(4.5f);

        // Convert DTO to JSON string
        String json = objectMapper.writeValueAsString(requestDto);

        // Setup expected feedback entity
        Feedback feedback = new Feedback();
        feedback.setId(1L);
        feedback.setContent("Great course feedback");

        // Setup expected API response
        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setMessage("Create feedback successfully");
        apiRes.setData(feedback);

        // Mock service method calls
        when(feedbackService.createFeedback(anyLong(), any(FeedbackType.class), any(CreateFeedbackDto.class)))
                .thenReturn(feedback);
        when(apiResUtil.returnApiRes(true, null, "Create feedback successfully", feedback))
                .thenReturn(apiRes);

        // Perform POST request and validate response
        mockMvc.perform(post("/feedback/create")
                        .param("courseId", "1")
                        .param("type", FeedbackType.REVIEW.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Create feedback successfully"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.content").value("Great course feedback"));
    }

    // Test for deleteFeedback endpoint
    @Test
    void testDeleteFeedback_Success() throws Exception {
        // Mocking the behavior of feedbackService.deleteFeedbackById
        doNothing().when(feedbackService).deleteFeedbackById(anyLong());

        // Performing the DELETE request
        mockMvc.perform(MockMvcRequestBuilders.delete("/feedback/delete")
                        .param("id", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().string("Delete feedback successfully"));
    }

    // Test for updateFeedback endpoint
    @Test
    void testUpdateFeedback_Success() throws Exception {
        // Create a request DTO
        CreateFeedbackDto requestDto = new CreateFeedbackDto();
        requestDto.setContent("Updated feedback content");
        requestDto.setRating(4.0f);

        // Convert DTO to JSON string
        String json = objectMapper.writeValueAsString(requestDto);

        // Define success message
        String successMessage = "Update feedback successfully";

        // Mock the service method to return a Feedback object
        Feedback updatedFeedback = new Feedback();
        updatedFeedback.setId(1L);  // Set some dummy values for testing
        updatedFeedback.setContent(requestDto.getContent());
        updatedFeedback.setCreatedBy("test_user");

        when(feedbackService.updateFeedbackById(anyLong(), any(CreateFeedbackDto.class)))
                .thenReturn(updatedFeedback);

        // Perform the PUT request and verify the response directly
        mockMvc.perform(put("/feedback/update?id=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(updatedFeedback.getId()))
                .andExpect(jsonPath("$.rating").value(updatedFeedback.getRating()))
                .andExpect(jsonPath("$.content").value(updatedFeedback.getContent()))
                .andExpect(jsonPath("$.createdBy").value(updatedFeedback.getCreatedBy()));
    }


    @Test
    void testGetFeedbackByCourseId_Success() throws Exception {
        Feedback feedback = new Feedback();
        feedback.setId(1L);
        feedback.setContent("Course feedback");

        List<Feedback> feedbackList = Collections.singletonList(feedback);

        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setData(feedbackList);

        // Mock service method to return feedback list
        when(feedbackService.getFeedbackByCourseIdAndType(anyLong(), any(FeedbackType.class)))
                .thenReturn(feedbackList);
        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
                .thenReturn(apiRes);

        mockMvc.perform(get("/feedback/view-by-course-id")
                        .param("courseId", "1")
                        .param("type", FeedbackType.REVIEW.name()) // Replace with actual enum value
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1))) // Ensure there is one element in the array
                .andExpect(jsonPath("$[0].id").value(feedback.getId())) // Check the id of the first element
                .andExpect(jsonPath("$[0].content").value(feedback.getContent())); // Check the content of the first element
    }

    @Test
    void testViewFeedbackByType_Success() throws Exception {
        Feedback feedback = new Feedback();
        feedback.setId(1L);
        feedback.setContent("Feedback by type");

        List<Feedback> feedbackList = Collections.singletonList(feedback);

        // Mocking the service method call
        when(feedbackService.getFeedbackByType(FeedbackType.REVIEW))
                .thenReturn(feedbackList);

        mockMvc.perform(get("/feedback/view-by-type")
                        .param("type", "REVIEW")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))  // Assuming the JSON array length
                .andExpect(jsonPath("$[0].id").value(feedback.getId()))
                .andExpect(jsonPath("$[0].content").value(feedback.getContent()));
    }
}
