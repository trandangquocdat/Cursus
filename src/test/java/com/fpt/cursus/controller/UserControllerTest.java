package com.fpt.cursus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.dto.object.StudiedCourse;
import com.fpt.cursus.dto.response.CustomAccountResDto;
import com.fpt.cursus.dto.response.GeneralCourse;
import com.fpt.cursus.dto.response.InstructorDashboardRes;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.enums.Role;
import com.fpt.cursus.service.AccountService;
import com.fpt.cursus.service.CourseService;
import com.fpt.cursus.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@WebMvcTest(UserController.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        AccountService.class,
        CourseService.class,
        DashboardService.class
})
class UserControllerTest {

    @MockBean
    private AccountService accountService;

    @MockBean
    private CourseService courseService;

    @MockBean
    private DashboardService dashboardService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new UserController(accountService, courseService, dashboardService))
                .alwaysDo(print())
                .build();
    }

    @Test
    void testSendCv() throws Exception {
        //given
        MockMultipartFile file = new MockMultipartFile("file",
                "test.txt",
                "text/plain",
                "test data".getBytes());
        Account account = new Account();
        account.setRole(Role.INSTRUCTOR);
        //when
        when(accountService.sendCv(any(MultipartFile.class))).thenReturn(account);
        //then
        mockMvc.perform(multipart("/send-cv")
                        .file(file))
                .andExpectAll(status().isOk(),
                        content().json(objectMapper.writeValueAsString(account)));
    }

//    @Test
//    void testGetInstructor() throws Exception {
//        //given
//        Account account = new Account();
//        account.setRole(Role.INSTRUCTOR);
//        List<Account> accounts = List.of(account);
//        //when
//        when(accountService.getInstructorByName(anyString())).thenReturn(accounts);
//        //then
//        mockMvc.perform(get("/view-instructor")
//                        .param("name", "name"))
//                .andExpectAll(status().isOk(),
//                        content().json(objectMapper.writeValueAsString(accounts)));
//    }

    @Test
    void testAddToWishList() throws Exception {
        //given
        CustomAccountResDto customAccountResDto = new CustomAccountResDto();
        customAccountResDto.setId(1L);
        customAccountResDto.setWishListCourses(List.of(1L, 2L));
        customAccountResDto.setStudiedCourses(List.of(new StudiedCourse()));
        customAccountResDto.setEnrolledCourses(List.of(1L, 2L));
        //when
        when(courseService.addToWishList(anyList())).thenReturn(customAccountResDto);
        //then
        mockMvc.perform(post("/wishlist/add")
                        .param("id", "1")
                        .param("id", "2"))
                .andExpectAll(status().isOk(),
                        content().json(objectMapper.writeValueAsString(customAccountResDto)));
    }

    @Test
    void testRemoveFromWishList() throws Exception {
        //given
        CustomAccountResDto customAccountResDto = new CustomAccountResDto();
        customAccountResDto.setWishListCourses(List.of(1L, 2L));
        //when
        when(courseService.removeFromWishList(anyLong())).thenReturn(customAccountResDto);
        //then
        mockMvc.perform(delete("/wishlist/remove")
                        .param("id", "3"))
                .andExpectAll(status().isOk(),
                        content().json(objectMapper.writeValueAsString(customAccountResDto)));
    }

    @Test
    void testViewWishList() throws Exception {
        //given
        Pageable pageable = PageRequest.of(1, 10, Sort.by("name"));
        List<GeneralCourse> generalCourses = List.of(new GeneralCourse());
        Page<GeneralCourse> page = new PageImpl<>(generalCourses, pageable, generalCourses.size());
        //when
        when(courseService.getWishListCourses(anyInt(), anyInt(), anyString())).thenReturn(page);
        //then
        mockMvc.perform(get("/wishlist/view")
                        .param("sortBy", "name")
                        .param("offset", "1")
                        .param("pageSize", "10"))
                .andExpectAll(status().isOk(),
                        content().json(objectMapper.writeValueAsString(page)));
    }

    @Test
    void testViewEnrolledCourses() throws Exception {
        //given
        Pageable pageable = PageRequest.of(1, 10, Sort.by("name"));
        List<GeneralCourse> generalCourses = List.of(new GeneralCourse());
        Page<GeneralCourse> page = new PageImpl<>(generalCourses, pageable, generalCourses.size());
        //when
        when(courseService.getGeneralEnrolledCourses(anyString(), anyInt(), anyInt())).thenReturn(page);
        //then
        mockMvc.perform(get("/enrolled-course/view-all-general")
                        .param("sortBy", "name")
                        .param("offset", "1")
                        .param("pageSize", "10"))
                .andExpectAll(status().isOk(),
                        content().json(objectMapper.writeValueAsString(page)));
    }

    @Test
    void testViewDetailEnrolledCourses() throws Exception {
        //given
        Pageable pageable = PageRequest.of(1, 10, Sort.by("name"));
        List<Course> courses = List.of(new Course());
        Page<Course> page = new PageImpl<>(courses, pageable, courses.size());
        //when
        when(courseService.getDetailEnrolledCourses(anyString(), anyInt(), anyInt())).thenReturn(page);
        //then
        mockMvc.perform(get("/enrolled-course/view-all-detail")
                        .param("sortBy", "name")
                        .param("offset", "1")
                        .param("pageSize", "10"))
                .andExpectAll(status().isOk(),
                        content().json(objectMapper.writeValueAsString(page)));
    }

    @Test
    void testViewMyCourse() throws Exception {
        //given
        Pageable pageable = PageRequest.of(1, 10, Sort.by("name"));
        List<Course> courses = List.of(new Course());
        Page<Course> page = new PageImpl<>(courses, pageable, courses.size());
        //when
        when(courseService.getCourseByCreatedBy(anyInt(), anyInt(), anyString())).thenReturn(page);
        //then
        mockMvc.perform(get("/view-my-course")
                        .param("sortBy", "name")
                        .param("offset", "1")
                        .param("pageSize", "10"))
                .andExpectAll(status().isOk(),
                        content().json(objectMapper.writeValueAsString(page)));
    }

    @Test
    void testSubscribeInstructor() throws Exception {
        // given
        long instructorId = 1L;
        // when
        doNothing().when(accountService).subscribeInstructor(anyLong()); // phương thức này không trả về giá trị gì
        // then
        mockMvc.perform(put("/subscribe-instructor")
                        .param("id", Long.toString(instructorId)))
                .andExpectAll(status().isOk(),
                        content().string("Subscribe successfully"));
    }

    @Test
    void testUnsubscribeInstructor() throws Exception {
        // given
        long instructorId = 1L;
        // when
        doNothing().when(accountService).unsubscribeInstructor(anyLong()); // phương thức này không trả về giá trị gì
        // then
        mockMvc.perform(put("/unsubscribe-instructor")
                        .param("id", Long.toString(instructorId)))
                .andExpectAll(status().isOk(),
                        content().string("Unsubscribe successfully"));
    }

    @Test
    void testGetProfile() throws Exception {
        // given
        Account account = new Account();
        account.setId(1L);
        account.setRole(Role.ADMIN);
        // when
        when(accountService.getProfile()).thenReturn(account);
        // then
        mockMvc.perform(get("/profile"))
                .andExpectAll(status().isOk(),
                        content().json(objectMapper.writeValueAsString(account)));
    }

    @Test
    void testGetInstructorDashboard() throws Exception {
        // given
        InstructorDashboardRes res = new InstructorDashboardRes();
        res.setCurrentSubscribers(1L);
        res.setTotalCourses(1L);
        res.setTotalEnroll(1L);
        res.setTotalSales(1.0);
        res.setTotalStudents(1L);
        // when
        when(dashboardService.getInstructorDashboardRes()).thenReturn(res);
        // then
        mockMvc.perform(get("/instructor-dashboard"))
                .andExpectAll(status().isOk(),
                        content().json(objectMapper.writeValueAsString(res)));
    }
}
