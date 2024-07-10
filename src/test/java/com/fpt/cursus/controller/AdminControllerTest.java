package com.fpt.cursus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.enums.CourseStatus;
import com.fpt.cursus.enums.InstructorStatus;
import com.fpt.cursus.enums.Role;
import com.fpt.cursus.service.AccountService;
import com.fpt.cursus.service.CourseService;
import com.fpt.cursus.util.ApiResUtil;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AdminController.class)
@ContextConfiguration(classes = {
        ApiResUtil.class,
        AccountService.class,
        CourseService.class
})
class AdminControllerTest {

    @MockBean
    private AccountService accountService;

    @MockBean
    private CourseService courseService;

    @MockBean
    private ApiResUtil apiResUtil;

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
    void testVerifyAccount_Success() throws Exception {
        //given
        InstructorStatus instructorStatus = InstructorStatus.APPROVED;
        long id = 1;
        String successMessage = "Verify instructor successfully.";
        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setStatus(null);
        apiRes.setMessage(successMessage);
        apiRes.setCode(null);
        apiRes.setData(null);
        //when
        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
                .thenReturn(apiRes);
        //then
        mockMvc.perform(patch("/admin/verify-instructor")
                        .param("id", String.valueOf(id))
                        .param("status", String.valueOf(instructorStatus)))
                .andExpectAll(status().isOk(),
                        jsonPath("$.message").value(successMessage));
    }

    @Test
    void testViewDraftCourse_Success() throws Exception {
        //given
        String sortBy = "name";
        int offset = 0;
        int limit = 10;
        Pageable pageable = PageRequest.of(offset, limit, Sort.Direction.DESC, sortBy);
        Course course = new Course();
        course.setName("course1");
        Course course2 = new Course();
        course2.setName("course2");
        List<Course> courseList = new ArrayList<>();
        courseList.add(course);
        courseList.add(course2);
        Page<Course> page = new PageImpl<>(courseList, pageable, courseList.size());
        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setStatus(null);
        apiRes.setMessage("");
        apiRes.setCode(null);
        apiRes.setData(page);
        //when
        when(courseService.getCourseByStatus(any(CourseStatus.class),
                anyInt(), anyInt(), anyString()))
                .thenReturn(page);
        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
                .thenReturn(apiRes);
        //then
        mockMvc.perform(get("/course/view-draft-course")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("sortBy", sortBy)
                        .param("offset", String.valueOf(offset))
                        .param("pageSize", String.valueOf(limit)))
                .andExpectAll(status().isOk(),
                        jsonPath("$.data").hasJsonPath());
    }

    @Test
    void testViewDraftCourse_NoParam_Success() throws Exception {
        //given
        String sortBy = "name";
        int offset = 0;
        int limit = 10;
        Pageable pageable = PageRequest.of(offset, limit, Sort.Direction.DESC, sortBy);
        Course course = new Course();
        course.setName("course1");
        Course course2 = new Course();
        course2.setName("course2");
        List<Course> courseList = new ArrayList<>();
        courseList.add(course);
        courseList.add(course2);
        Page<Course> page = new PageImpl<>(courseList, pageable, courseList.size());
        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setStatus(null);
        apiRes.setMessage("");
        apiRes.setCode(null);
        apiRes.setData(page);
        //when
        when(courseService.getCourseByStatus(any(CourseStatus.class),
                anyInt(), anyInt(), anyString()))
                .thenReturn(page);
        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
                .thenReturn(apiRes);
        //then
        mockMvc.perform(get("/course/view-draft-course")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.data").hasJsonPath());
    }

    @Test
    void testVerifyCourse_Success() throws Exception {
        //given
        CourseStatus courseStatus = CourseStatus.PUBLISHED;
        long id = 1;
        String successMessage = "Verify course successfully.";
        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setStatus(null);
        apiRes.setMessage(successMessage);
        apiRes.setCode(null);
        apiRes.setData(null);
        //when
        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
                .thenReturn(apiRes);
        //then
        mockMvc.perform(patch("/admin/verify-course")
                        .param("id", String.valueOf(id))
                        .param("status", String.valueOf(courseStatus)))
                .andExpectAll(status().isOk(),
                        jsonPath("$.message").value(successMessage));
    }

    @Test
    void testViewVerifyingInstructor_Success() throws Exception {
        //given
        InstructorStatus instructorStatus = InstructorStatus.APPROVED;

        Account account = new Account();
        account.setUsername("username");
        account.setRole(Role.INSTRUCTOR);
        account.setInstructorStatus(instructorStatus);
        Account account2 = new Account();
        account2.setUsername("username2");
        account2.setRole(Role.INSTRUCTOR);
        account2.setInstructorStatus(instructorStatus);
        List<Account> accountList = new ArrayList<>();
        accountList.add(account);
        accountList.add(account2);

        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setStatus(null);
        apiRes.setMessage(null);
        apiRes.setCode(null);
        apiRes.setData(accountList);
        //when
        when(accountService.getInstructorByInstStatus(any(InstructorStatus.class)))
                .thenReturn(accountList);
        when(apiResUtil.returnApiRes(any(), any(), any(), anyList()))
                .thenReturn(apiRes);
        //then
        mockMvc.perform(get("/admin/view-verifying-instructor")
                        .param("status", String.valueOf(instructorStatus)))
                .andExpectAll(status().isOk(),
                        jsonPath("$.data").hasJsonPath());
    }

    @Test
    void testDeleteAccount_Success() throws Exception {
        //given
        String username = "username";
        String successMessage = "Delete account successfully.";
        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setStatus(null);
        apiRes.setMessage(successMessage);
        apiRes.setCode(null);
        apiRes.setData(null);
        //when
        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
                .thenReturn(apiRes);
        //then
        mockMvc.perform(delete("/admin/delete-account")
                        .param("username", username))
                .andExpectAll(status().isOk(),
                        jsonPath("$.message").value(successMessage));
    }

    @Test
    void testSetAdmin_Success() throws Exception {
        //given
        String email = "email";
        String successMessage = "Set admin role successfully.";
        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setStatus(null);
        apiRes.setMessage(successMessage);
        apiRes.setCode(null);
        apiRes.setData(null);
        //when
        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
                .thenReturn(apiRes);
        //then
        mockMvc.perform(patch("/admin/set-admin")
                        .param("email", email))
                .andExpectAll(status().isOk(),
                        jsonPath("$.message").value(successMessage));
    }
}
