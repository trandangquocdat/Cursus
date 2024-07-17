package com.fpt.cursus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.dto.request.CreateCourseDto;
import com.fpt.cursus.dto.request.UpdateCourseDto;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.enums.Category;
import com.fpt.cursus.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CourseControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CourseService courseService;

    @InjectMocks
    private CourseController courseController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(courseController).build();
    }

    @Test
    void createCourse_success() throws Exception {
        CreateCourseDto createCourseDto = new CreateCourseDto();
        createCourseDto.setName("Test Course");
        createCourseDto.setCategory(Category.FINANCE);
        createCourseDto.setDescription("This is a test course.");
        createCourseDto.setPrice(100.0);

        Course course = new Course();
        course.setId(1L);
        course.setName("Test Course");

        when(courseService.createCourse(any(CreateCourseDto.class))).thenReturn(course);

        mockMvc.perform(multipart("/course/create")
                        .flashAttr("createCourseDto", createCourseDto))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Course"));
    }

    @Test
    void updateCourse_success() throws Exception {
        UpdateCourseDto updateCourseDto = new UpdateCourseDto();
        updateCourseDto.setName("Updated Course");
        updateCourseDto.setDescription("Updated description");

        Course updatedCourse = new Course();
        updatedCourse.setId(1L);
        updatedCourse.setName("Updated Course");

        when(courseService.updateCourse(anyLong(), any(UpdateCourseDto.class))).thenReturn(updatedCourse);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/course/update")
                        .file("pictureLink", new byte[0]) // Simulate an empty file
                        .param("id", "1")
                        .param("name", "Updated Course")
                        .param("description", "Updated description")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Course"));
    }

    @Test
    void deleteCourse_success() throws Exception {
        Course course = new Course();
        when(courseService.deleteCourseById(anyLong())).thenReturn(course);

        mockMvc.perform(delete("/course/delete")
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }


    @Test
    void viewAllGeneralCourse_success() throws Exception {
        when(courseService.getAllGeneralCourses(anyString(), any(Integer.class), any(Integer.class)))
                .thenReturn(Page.empty()); // Adjust to return a list of courses if needed

        mockMvc.perform(MockMvcRequestBuilders.get("/course/view-all-general")
                        .param("sortBy", "name")
                        .param("offset", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void viewCourseByCategory_success() throws Exception {
        when(courseService.getCourseByCategory(any(Category.class), any(Integer.class), any(Integer.class), anyString()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/course/view-general-by-category")
                        .param("category", "ALL")
                        .param("sortBy", "name")
                        .param("offset", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void viewCourseByName_success() throws Exception {
        // Mocking the service call
        when(courseService.getGeneralCourseByName(anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn(Page.empty()); // Adjust this to return a valid Page if needed

        // Performing the mockMvc request
        mockMvc.perform(get("/course/view-general-by-name")
                        .param("name", "Test Course")
                        .param("sortBy", "name")
                        .param("offset", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
