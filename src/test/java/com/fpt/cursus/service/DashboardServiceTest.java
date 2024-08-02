package com.fpt.cursus.service;

import com.fpt.cursus.dto.response.InstructorDashboardRes;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.entity.Orders;
import com.fpt.cursus.entity.OrdersDetail;
import com.fpt.cursus.enums.OrderStatus;
import com.fpt.cursus.service.impl.DashboardServiceImpl;
import com.fpt.cursus.util.AccountUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
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

    @BeforeEach
    void setUp() {
        //given
        Account account = new Account();
        account.setUsername("test");
        Course course = new Course();
        course.setId(1L);
        course.setEnroller(1L);
        List<Course> courses = List.of(course);

        Orders orders = new Orders();
        orders.setId(1L);

        OrdersDetail ordersDetail = new OrdersDetail();
        ordersDetail.setId(1L);
        ordersDetail.setPrice(100.0);
        ordersDetail.setStatus(OrderStatus.PAID);
        ordersDetail.setCourseId(1L);
        ordersDetail.setCreatedBy("test");
        ordersDetail.setCreatedDate(new Date());
        ordersDetail.setOrders(orders);
        OrdersDetail ordersDetail2 = new OrdersDetail();
        ordersDetail2.setId(2L);
        ordersDetail2.setPrice(100.0);
        ordersDetail2.setStatus(OrderStatus.PENDING);
        List<OrdersDetail> ordersDetails = new ArrayList<>();
        ordersDetails.add(ordersDetail);
        ordersDetails.add(ordersDetail2);
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(courseService.getCourseByCreatedBy(anyString())).thenReturn(courses);
        when(orderService.findAllByCourseIdIn(anyList())).thenReturn(ordersDetails);
        when(accountService.getSubscribersUsers(any(Account.class))).thenReturn(List.of(1L));
    }

    @Test
    void testGetInstructorDashboardRes() {
        InstructorDashboardRes res = dashboardService.getInstructorDashboardRes();
        assertEquals(1, res.getTotalCourses());
        assertEquals(1, res.getTotalStudents());
        assertEquals(1, res.getTotalEnroll());
        assertEquals(100.0, res.getTotalSales());
        assertEquals(1, res.getCurrentSubscribers());
    }

}
