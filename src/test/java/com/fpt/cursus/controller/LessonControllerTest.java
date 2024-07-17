package com.fpt.cursus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.dto.request.CreateLessonDto;
import com.fpt.cursus.entity.Lesson;
import com.fpt.cursus.enums.LessonStatus;
import com.fpt.cursus.service.LessonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(SpringExtension.class)
@WebMvcTest(LessonController.class)
@ContextConfiguration(classes = {
        LessonService.class,
})
class LessonControllerTest {

    @MockBean
    private LessonService lessonService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new LessonController(lessonService))
                .alwaysDo(print())
                .build();
    }

    @Test
    void createLessonSuccess() throws Exception {
        //given
        MockMultipartFile video = new MockMultipartFile("videoLink",
                "video.mp4",
                "video/mp4",
                "video".getBytes());

        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setName("Lesson 1");
        lesson.setDescription("Description");
        lesson.setVideoLink("video.mp4");
        //when
        when(lessonService.createLesson(anyLong(), any(CreateLessonDto.class)))
                .thenReturn(lesson);
        //then
        mockMvc.perform(multipart("/lesson/create")
                        .file(video)
                        .param("chapterId", "1")
                        .param("name", "Lesson 1")
                        .param("description", "Description"))
                .andExpectAll(status().isCreated(),
                        content().json(mapper.writeValueAsString(lesson)));
    }

    @Test
    void uploadExcelFileSuccess() throws Exception {
        //given
        MockMultipartFile file = new MockMultipartFile("file",
                "file.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "file".getBytes());
        List<String> list = new ArrayList<>();
        list.add("Lesson 1");
        list.add("Lesson 2");
        //when
        when(lessonService.uploadLessonFromExcel(anyLong(), any(MultipartFile.class)))
                .thenReturn(list);
        //then
        mockMvc.perform(multipart("/lesson/upload-excel")
                        .file(file)
                        .param("chapterId", "1"))
                .andExpectAll(status().isOk(),
                        content().json(mapper.writeValueAsString(list)));
    }

    @Test
    void updateLessonSuccess() throws Exception {
        //given
        MockMultipartFile video = new MockMultipartFile("videoLink",
                "video.mp4",
                "video/mp4",
                "video".getBytes());

        Lesson lesson = new Lesson();
        lesson.setName("Lesson 1");
        lesson.setDescription("Description");
        //when
        when(lessonService.updateLesson(anyLong(), any(CreateLessonDto.class)))
                .thenReturn(lesson);
        //then
        mockMvc.perform(put("/lesson/update", video)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("lessonId", "1")
                        .param("name", "Lesson 1")
                        .param("description", "Description"))
                .andExpectAll(status().isOk(),
                        content().json(mapper.writeValueAsString(lesson)));
    }

    @Test
    void deleteLessonSuccess() throws Exception {
        //given
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setStatus(LessonStatus.DELETED);
        //when
        when(lessonService.deleteLessonById(anyLong()))
                .thenReturn(lesson);
        //then
        mockMvc.perform(delete("/lesson/delete")
                        .param("lessonId", "1"))
                .andExpectAll(status().isOk(),
                        content().json(mapper.writeValueAsString(lesson)));
    }

    @Test
    void findAllSuccess() throws Exception {
        //given
        List<Lesson> lessons = new ArrayList<>();
        Lesson lesson1 = new Lesson();
        lesson1.setId(1L);
        Lesson lesson2 = new Lesson();
        lesson2.setId(2L);
        lessons.add(lesson1);
        lessons.add(lesson2);
        //when
        when(lessonService.findAll())
                .thenReturn(lessons);
        //then
        mockMvc.perform(get("/lesson/get-all"))
                .andExpectAll(status().isOk(),
                        content().json(mapper.writeValueAsString(lessons)));
    }

    @Test
    void findByIdSuccess() throws Exception {
        //given
        List<Lesson> lessons = new ArrayList<>();
        Lesson lesson1 = new Lesson();
        lesson1.setId(1L);
        Lesson lesson2 = new Lesson();
        lesson2.setId(2L);
        lessons.add(lesson1);
        lessons.add(lesson2);
        //when
        when(lessonService.findAllByChapterId(anyLong()))
                .thenReturn(lessons);
        //then
        mockMvc.perform(get("/lesson/get-by-chapter")
                        .param("chapterId", "1"))
                .andExpectAll(status().isOk(),
                        content().json(mapper.writeValueAsString(lessons)));
    }
}
