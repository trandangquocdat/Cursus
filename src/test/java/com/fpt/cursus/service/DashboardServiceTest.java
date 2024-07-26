package com.fpt.cursus.service;

import com.fpt.cursus.dto.object.StudiedCourse;
import com.fpt.cursus.dto.response.InstructorDashboardRes;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.entity.Orders;
import com.fpt.cursus.entity.OrdersDetail;
import com.fpt.cursus.service.impl.DashboardServiceImpl;
import com.fpt.cursus.util.AccountUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Mock
    private AccountUtil accountUtil;

    @Mock
    private CourseService courseService;

    @Mock
    private OrderService orderService;

    @Mock
    private AccountService accountService;

    @Test
    void testGetInstructorDashboardRes() {
        //given
        Account account = new Account();
        account.setUsername("username");

        List<Course> courses = new ArrayList<>();
        Course course = new Course();
        course.setId(1L);
        courses.add(course);

        List<OrdersDetail> ordersDetails = new ArrayList<>();
        OrdersDetail ordersDetail = new OrdersDetail();
        ordersDetail.setPrice(100.0);
        Orders orders = new Orders();
        orders.setCreatedBy("username");
        ordersDetail.setOrders(orders);
        ordersDetails.add(ordersDetail);

        List<StudiedCourse> studiedCourses = new ArrayList<>();
        StudiedCourse studiedCourse = new StudiedCourse();
        studiedCourse.setCourseId(1L);
        studiedCourses.add(studiedCourse);

        List<Long> subscribers = List.of(1L, 2L);
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(courseService.getCourseByCreatedBy(anyString())).thenReturn(courses);
        when(orderService.findAllByIdIn(anyList())).thenReturn(ordersDetails);
        when(accountService.getAccountByUsername(anyString())).thenReturn(account);
        when(courseService.getStudiedCourses(account)).thenReturn(studiedCourses);
        when(accountService.getSubscribersUsers(any(Account.class))).thenReturn(subscribers);
        //then
        InstructorDashboardRes res = dashboardService.getInstructorDashboardRes();
        assertEquals(1, res.getTotalCourses());
        assertEquals(100.0, res.getTotalSales());
        assertEquals(1, res.getTotalStudents());
        assertEquals(0, res.getTotalEnroll());
        assertEquals(2, res.getCurrentSubscribers());
    }

    @Test
    void testGetInstructorDashboardResWithStudiedCourses() {
        //given
        Account account = new Account();
        account.setUsername("username");

        List<Course> courses = new ArrayList<>();
        Course course = new Course();
        course.setId(1L);
        courses.add(course);

        List<OrdersDetail> ordersDetails = new ArrayList<>();
        OrdersDetail ordersDetail = new OrdersDetail();
        ordersDetail.setPrice(100.0);
        Orders orders = new Orders();
        orders.setCreatedBy("username");
        ordersDetail.setOrders(orders);
        ordersDetails.add(ordersDetail);

        List<StudiedCourse> studiedCourses = new ArrayList<>();
        StudiedCourse studiedCourse = new StudiedCourse();
        studiedCourse.setCourseId(1L);
        StudiedCourse studiedCourse2 = new StudiedCourse();
        studiedCourse2.setCourseId(2L);
        studiedCourses.add(studiedCourse);
        studiedCourses.add(studiedCourse2);

        List<Long> subscribers = List.of(1L, 2L);
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(courseService.getCourseByCreatedBy(anyString())).thenReturn(courses);
        when(orderService.findAllByIdIn(anyList())).thenReturn(ordersDetails);
        when(accountService.getAccountByUsername(anyString())).thenReturn(account);
        when(courseService.getStudiedCourses(account)).thenReturn(studiedCourses);
        when(accountService.getSubscribersUsers(any(Account.class))).thenReturn(subscribers);
        //then
        InstructorDashboardRes res = dashboardService.getInstructorDashboardRes();
        assertEquals(1, res.getTotalCourses());
        assertEquals(100.0, res.getTotalSales());
        assertEquals(1, res.getTotalStudents());
        assertEquals(1, res.getTotalEnroll());
        assertEquals(2, res.getCurrentSubscribers());
    }
}
