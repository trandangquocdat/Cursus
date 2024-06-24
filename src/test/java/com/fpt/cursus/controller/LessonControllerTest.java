package com.fpt.cursus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.dto.request.CreateLessonDto;
import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.entity.Lesson;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new LessonController(apiResUtil, lessonService))
                .alwaysExpect(status().isOk())
                .build();
    }

    @Test
    void testCreateLesson() throws Exception {
        CreateLessonDto createLessonDto = new CreateLessonDto();
        createLessonDto.setName("Lesson 1");
        createLessonDto.setDescription("Content of lesson 1");
        createLessonDto.setVideoLink("https://www.youtube.com/watch?v=video1");

        String json = mapper.writeValueAsString(createLessonDto);

        Lesson lesson = new Lesson();
        lesson.setName(createLessonDto.getName());
        lesson.setDescription(createLessonDto.getDescription());
        lesson.setVideoLink(createLessonDto.getVideoLink());

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
                        jsonPath("$.data.videoLink").value("https://www.youtube.com/watch?v=video1"))
                .andDo(print());
    }
}
