package com.fpt.cursus.controller;

import com.fpt.cursus.service.impl.CourseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@WebMvcTest(controllers = CourseController.class)
@AutoConfigureMockMvc
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseController controller;

    @Autowired
    private CourseServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {

    }

}
