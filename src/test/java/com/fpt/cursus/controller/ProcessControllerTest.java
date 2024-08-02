package com.fpt.cursus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.dto.object.StudiedCourse;
import com.fpt.cursus.dto.response.CustomAccountResDto;
import com.fpt.cursus.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@WebMvcTest(ProcessController.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        CourseService.class
})
class ProcessControllerTest {

    @MockBean
    private CourseService courseService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new ProcessController(courseService))
                .alwaysExpect(status().isOk())
                .alwaysDo(print())
                .build();
    }

    @Test
    void addStudiedLesson_ReturnsOk() throws Exception {
        //given
        CustomAccountResDto customAccountResDto = new CustomAccountResDto();
        customAccountResDto.setId(1L);
        customAccountResDto.setWishListCourses(new ArrayList<>());
        customAccountResDto.setStudiedCourses(new ArrayList<>());
        customAccountResDto.setEnrolledCourses(new ArrayList<>());
        //when
        when(courseService.addStudiedLesson(anyLong())).thenReturn(customAccountResDto);
        //then
        mockMvc.perform(put("/process/add-studied-lesson")
                        .param("lessonId", "1"))
                .andExpect(content().json(objectMapper.writeValueAsString(customAccountResDto)));
    }

    @Test
    void percentDoneCourse_ReturnsOk() throws Exception {
        //when
        when(courseService.percentDoneCourse(anyLong())).thenReturn(75.0);
        //then
        mockMvc.perform(get("/process/percent-done")
                        .param("courseId", "1"))
                .andExpect(content().string("75.0"));
    }

    @Test
    void viewAllStudiedLesson_ReturnsOk() throws Exception {
        //given
        List<StudiedCourse> list = new ArrayList<>();
        //when
        when(courseService.getAllStudiedCourses()).thenReturn(list);
        //then
        mockMvc.perform(get("/process/view-all-studied-lesson"))
                .andExpect(content().json(objectMapper.writeValueAsString(list)));
    }

    @Test
    void viewLastStudiedLesson_ReturnsOk() throws Exception {
        //given
        StudiedCourse expectedResult = new StudiedCourse(); // Mock expected result
        //when
        when(courseService.getCheckPoint()).thenReturn(expectedResult);
        //then
        mockMvc.perform(get("/process/view-checkpoint"))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResult)));
    }
}
