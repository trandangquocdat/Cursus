package com.fpt.cursus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.enums.CourseStatus;
import com.fpt.cursus.enums.InstructorStatus;
import com.fpt.cursus.enums.Role;
import com.fpt.cursus.enums.UserStatus;
import com.fpt.cursus.service.AccountService;
import com.fpt.cursus.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AdminController.class)
@ContextConfiguration(classes = {
        AccountService.class,
        CourseService.class
})
class AdminControllerTest {

    @MockBean
    private AccountService accountService;

    @MockBean
    private CourseService courseService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new AdminController(accountService, courseService))
                .alwaysDo(print())
                .build();
    }

    @Test
    void approveInstructorSuccess() throws Exception {
        //given
        Account account = new Account();
        account.setId(1L);
        account.setRole(Role.INSTRUCTOR);
        //when
        when(accountService.approveInstructorById(anyLong(), any(InstructorStatus.class)))
                .thenReturn(account);
        //then
        mockMvc.perform(patch("/admin/approve-instructor")
                        .param("id", "1")
                        .param("status", InstructorStatus.APPROVED.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().json(objectMapper.writeValueAsString(account)));
    }

    @Test
    void viewDraftCourseSuccess() throws Exception {
        //given
        List<Course> courses = new ArrayList<>();
        Course course = new Course();
        course.setId(1L);
        course.setStatus(CourseStatus.DRAFT);
        courses.add(course);

        Pageable pageable = PageRequest.of(1, 10, Sort.by("id"));
        Page<Course> page = new PageImpl<>(courses, pageable, courses.size());
        //when
        when(courseService.getCourseByStatus(any(CourseStatus.class), anyInt(), anyInt(), anyString()))
                .thenReturn(page);
        //then
        mockMvc.perform(get("/admin/view-draft-course")
                        .param("sortBy", "id")
                        .param("offset", "1")
                        .param("pageSize", "10"))
                .andExpectAll(status().isOk(),
                        content().json(objectMapper.writeValueAsString(page)));
    }

    @Test
    void viewAllCourseSuccess() throws Exception {
        //given
        List<Course> courses = new ArrayList<>();
        Course course = new Course();
        course.setId(1L);
        courses.add(course);

        Pageable pageable = PageRequest.of(1, 10, Sort.by("id"));
        Page<Course> page = new PageImpl<>(courses, pageable, courses.size());
        //when
        when(courseService.getAllCourse(anyInt(), anyInt(), anyString()))
                .thenReturn(page);
        //then
        mockMvc.perform(get("/admin/view-all-course")
                        .param("sortBy", "id")
                        .param("offset", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().json(objectMapper.writeValueAsString(page)));
    }

    @Test
    void approveCourseSuccess() throws Exception {
        //given
        Course course = new Course();
        course.setId(1L);
        course.setStatus(CourseStatus.PUBLISHED);
        //when
        when(courseService.approveCourseById(anyLong(), any(CourseStatus.class)))
                .thenReturn(course);
        //then
        mockMvc.perform(patch("/admin/approve-course")
                        .param("id", "1")
                        .param("status", CourseStatus.PUBLISHED.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().json(objectMapper.writeValueAsString(course)));
    }

    @Test
    void viewVerifyingInstructorSuccess() throws Exception {
        //given
        List<Account> accounts = new ArrayList<>();
        Account account = new Account();
        account.setId(1L);
        account.setRole(Role.INSTRUCTOR);
        accounts.add(account);
        //when
        when(accountService.getInstructorByInstStatus(any(InstructorStatus.class)))
                .thenReturn(accounts);
        //then
        mockMvc.perform(get("/admin/view-instructor")
                        .param("status", InstructorStatus.APPROVED.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().json(objectMapper.writeValueAsString(accounts)));
    }

    @Test
    void viewListSuccess() throws Exception {
        //given
        List<Account> accounts = new ArrayList<>();
        Account account = new Account();
        account.setId(1L);
        account.setRole(Role.INSTRUCTOR);
        accounts.add(account);
        Pageable pageable = PageRequest.of(1, 10, Sort.by("id"));
        Page<Account> page = new PageImpl<>(accounts, pageable, accounts.size());
        //when
        when(accountService.getListOfStudentAndInstructor(any(Role.class), anyInt(), anyInt(), anyString()))
                .thenReturn(page);
        //then
        mockMvc.perform(get("/admin/view-instructor-and-student")
                        .param("role", Role.INSTRUCTOR.toString())
                        .param("sortBy", "id")
                        .param("offset", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().json(objectMapper.writeValueAsString(page)));
    }

    @Test
    void setStatusAccountSuccess() throws Exception {
        //given
        Account account = new Account();
        account.setId(1L);
        account.setRole(Role.STUDENT);
        account.setStatus(UserStatus.ACTIVE);
        //when
        when(accountService.setStatusAccount(anyString(), any(UserStatus.class)))
                .thenReturn(account);
        //then
        mockMvc.perform(delete("/admin/set-status-account")
                        .param("username", "username")
                        .param("status", UserStatus.ACTIVE.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().json(objectMapper.writeValueAsString(account)));
    }

    @Test
    void setAdminSuccess() throws Exception {
        //given
        Account account = new Account();
        account.setId(1L);
        account.setEmail("test@test.com");
        account.setRole(Role.ADMIN);
        //when
        when(accountService.setAdmin(anyString()))
                .thenReturn(account);
        //then
        mockMvc.perform(patch("/admin/set-admin")
                        .param("email", "test@test.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().json(objectMapper.writeValueAsString(account)));
    }
}
