package com.fpt.cursus.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.dto.request.PaymentDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.entity.Orders;
import com.fpt.cursus.enums.status.OrderStatus;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.OrdersRepo;
import com.fpt.cursus.service.impl.CourseServiceImpl;
import com.fpt.cursus.service.impl.EnrollCourseServiceImpl;
import com.fpt.cursus.util.AccountUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    OrderService orderService;

    @Mock
    AccountUtil accountUtil;

    @Mock
    OrdersRepo ordersRepo;

    @Mock
    EnrollCourseServiceImpl enrollCourseService;

    @Mock
    CourseServiceImpl courseService;

    @Mock
    ObjectMapper mapper;

    private Orders orders;
    private Account account;
    private PaymentDto paymentDto;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setUsername("admin");

        orders = new Orders();
        orders.setId(1L);

        paymentDto = new PaymentDto();
    }

    @Test
    void testSaveOrderSuccess() {
        double price = 100.0;
        List<Long> courseIds = new ArrayList<>();
        courseIds.add(1L);
        courseIds.add(2L);
        String username = account.getUsername();

        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(ordersRepo.save(any(Orders.class))).thenReturn(orders);

        orderService.saveOrder(orders, courseIds, price);

        assertEquals(username, orders.getCreatedBy());
        assertNotNull(orders.getCreatedDate());
        assertEquals(OrderStatus.PENDING, orders.getStatus());
        assertEquals(price, orders.getPrice());
        assertNotNull(orders.getOrderCourseJson());
    }

    @Test
    void testOrderSuccess() throws JsonProcessingException {
        orders.setOrderCourseJson("[1,2]");
        orders.setStatus(OrderStatus.PENDING);

        when(ordersRepo.findOrdersById(anyLong())).thenReturn(orders);
        when(ordersRepo.save(any(Orders.class))).thenReturn(orders);

        orderService.orderSuccess(1L);

        verify(enrollCourseService, times(1)).enrollCourseAfterPay(anyList());

        assertEquals(OrderStatus.PAID, orders.getStatus());
        assertNotNull(orders.getOrderCourse());
    }

    @Test
    void testOrderFailure() throws JsonProcessingException {
        orders.setOrderCourseJson("[1,2]");
        orders.setStatus(OrderStatus.PENDING);

        when(ordersRepo.findOrdersById(anyLong())).thenReturn(orders);
        doThrow(JsonProcessingException.class).when(enrollCourseService).enrollCourseAfterPay(anyList());

        assertThrows(AppException.class, () -> orderService.orderSuccess(1L),
                ErrorCode.ORDER_FAIL.getMessage());
    }

    @Test
    void testCreateUrlSuccess() {
        List<Long> list = new ArrayList<>();
        list.add(1L);
        paymentDto.setCourseId(list);

        Course course = new Course();
        course.setPrice(100.0);

        when(courseService.findCourseById(anyLong())).thenReturn(course);
        when(ordersRepo.save(any(Orders.class))).thenReturn(orders);
        when(accountUtil.getCurrentAccount()).thenReturn(account);

        ResponseEntity<String> response = orderService.createUrl(paymentDto);

        assertNotNull(response);
        assertTrue(Objects.requireNonNull(
                response.getBody()).contains("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"));
    }

    @Test
    void testCreateUrlEmptyCourseList() {
        assertThrows(AppException.class, () -> orderService.createUrl(paymentDto),
                ErrorCode.ORDER_CART_NULL.getMessage());
    }

    @Test
    void testCreateUrlCourseNotFound() {
        List<Long> list = new ArrayList<>();
        list.add(1L);
        paymentDto.setCourseId(list);
        when(courseService.findCourseById(anyLong())).thenReturn(null);
        assertThrows(AppException.class, () -> orderService.createUrl(paymentDto),
                ErrorCode.COURSE_NOT_FOUND.getMessage());
    }
}
