package com.fpt.cursus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.dto.request.CreateLessonDto;
import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.entity.Lesson;
import com.fpt.cursus.enums.status.LessonStatus;
import com.fpt.cursus.service.LessonService;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(SpringExtension.class)
@WebMvcTest(LessonController.class)
@ContextConfiguration(classes = {LessonController.class,
        ApiResUtil.class,
        LessonService.class
})
class LessonControllerTest {

    @MockBean
    private LessonService lessonService;

    @MockBean
    private ApiResUtil apiResUtil;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private CreateLessonDto createLessonDto;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        createLessonDto = new CreateLessonDto();
        createLessonDto.setName("Lesson 1");
        createLessonDto.setDescription("Content of lesson 1");
        createLessonDto.setVideoLink("https://www.youtube.com/watch?v=video1");

        lesson = new Lesson();
        lesson.setId(1);
        lesson.setName(createLessonDto.getName());
        lesson.setDescription(createLessonDto.getDescription());
        lesson.setStatus(LessonStatus.ACTIVE);
        lesson.setVideoLink(createLessonDto.getVideoLink());
        lesson.setCreatedDate(new Date());
        lesson.setUpdatedDate(new Date());
        lesson.setCreatedBy("admin");
        lesson.setUpdatedBy("admin");

        mockMvc = standaloneSetup(new LessonController(apiResUtil, lessonService))
                .alwaysDo(print())
                .alwaysExpect(status().isOk())
                .build();
    }

    @Test
    void testCreateLesson() throws Exception {
        String json = mapper.writeValueAsString(createLessonDto);

        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setCode(200);
        apiRes.setStatus(true);
        apiRes.setMessage("Success");
        apiRes.setData(lesson);

        when(lessonService.createLesson(anyLong(), any(CreateLessonDto.class)))
                .thenReturn(lesson);
        when(apiResUtil.returnApiRes(null, null, null, lesson))
                .thenReturn(apiRes);

        mockMvc.perform(post("/lesson/create")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .param("chapterId", "1"))
                .andExpectAll(jsonPath("$.data.name").value("Lesson 1"),
                        jsonPath("$.data.description").value("Content of lesson 1"),
                        jsonPath("$.data.videoLink").value("https://www.youtube.com/watch?v=video1"));
    }

    @Test
    void testUpdateLesson() throws Exception {
        String json = mapper.writeValueAsString(createLessonDto);

        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setCode(null);
        apiRes.setStatus(null);
        apiRes.setMessage("Success");
        apiRes.setData(null);

        doNothing().when(lessonService).updateLesson(anyLong(), any(CreateLessonDto.class));
        when(apiResUtil.returnApiRes(any(), any(), anyString(), any()))
                .thenReturn(apiRes);

        mockMvc.perform(put("/lesson/update")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .param("lessonId", "1"))
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    void testDeleteLesson() throws Exception {
        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setCode(null);
        apiRes.setStatus(null);
        apiRes.setMessage("Success");
        apiRes.setData(null);

        doNothing().when(lessonService).deleteLessonById(anyLong());
        when(apiResUtil.returnApiRes(any(), any(), anyString(), any()))
                .thenReturn(apiRes);

        mockMvc.perform(delete("/lesson/delete")
                        .param("lessonId", "1"))
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    void testGetAllLesson() throws Exception {
        List<Lesson> lessonList = new ArrayList<>();
        lessonList.add(lesson);

        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setCode(null);
        apiRes.setStatus(null);
        apiRes.setMessage(null);
        apiRes.setData(lessonList);

        when(lessonService.findAll()).thenReturn(lessonList);
        when(apiResUtil.returnApiRes(any(), any(), any(), anyList()))
                .thenReturn(apiRes);

        mockMvc.perform(get("/lesson/get-all"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void testGetAllByChapterId() throws Exception {
        List<Lesson> lessonList = new ArrayList<>();
        lessonList.add(lesson);

        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setCode(null);
        apiRes.setStatus(null);
        apiRes.setMessage(null);
        apiRes.setData(lessonList);

        when(lessonService.findAllByChapterId(anyLong()))
                .thenReturn(lessonList);
        when(apiResUtil.returnApiRes(any(), any(), any(), anyList()))
                .thenReturn(apiRes);

        mockMvc.perform(get("/lesson/get-by-chapter")
                        .param("chapterId", "1"))
                .andExpect(jsonPath("$.data").exists());
    }
}
