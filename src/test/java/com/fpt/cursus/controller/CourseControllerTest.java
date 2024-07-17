package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.CreateCourseDto;
import com.fpt.cursus.dto.response.GeneralCourse;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.enums.Category;
import com.fpt.cursus.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
        GeneralCourse newGeneralCourse = new GeneralCourse();
        newGeneralCourse.setId(1L);
        List<GeneralCourse> list = new ArrayList<>();
        list.add(newGeneralCourse);
        Pageable pageable = PageRequest.of(1, 10, Sort.by("name"));
        Page<GeneralCourse> generalCoursePage = new PageImpl<>(list, pageable, list.size());


        when(courseService.getAllGeneralCourses(anyString(), anyInt(), anyInt()))
                .thenReturn(generalCoursePage); // Adjust to return a list of courses if needed

        mockMvc.perform(MockMvcRequestBuilders.get("/course/view-all-general")
                        .param("sortBy", "name")
                        .param("offset", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void viewCourseByCategory_success() throws Exception {
        GeneralCourse newGeneralCourse = new GeneralCourse();
        newGeneralCourse.setId(1L);
        List<GeneralCourse> list = new ArrayList<>();
        list.add(newGeneralCourse);
        Pageable pageable = PageRequest.of(1, 10, Sort.by("name"));
        Page<GeneralCourse> generalCoursePage = new PageImpl<>(list, pageable, list.size());
        when(courseService.getCourseByCategory(any(Category.class), any(Integer.class), any(Integer.class), anyString()))
                .thenReturn(generalCoursePage);

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
        GeneralCourse newGeneralCourse = new GeneralCourse();
        newGeneralCourse.setId(1L);
        List<GeneralCourse> list = new ArrayList<>();
        list.add(newGeneralCourse);
        Pageable pageable = PageRequest.of(1, 10, Sort.by("name"));
        Page<GeneralCourse> generalCoursePage = new PageImpl<>(list, pageable, list.size());
        when(courseService.getGeneralCourseByName(anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn(generalCoursePage); // Adjust this to return a valid Page if needed

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
