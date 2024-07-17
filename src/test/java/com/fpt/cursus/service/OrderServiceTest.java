package com.fpt.cursus.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.dto.request.PaymentDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.entity.Orders;
import com.fpt.cursus.enums.OrderStatus;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.OrdersRepo;
import com.fpt.cursus.service.impl.OrderServiceImpl;
import com.fpt.cursus.util.AccountUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @InjectMocks
    OrderServiceImpl orderService;

    @Mock
    AccountUtil accountUtil;

    @Mock
    OrdersRepo ordersRepo;

    @Mock
    CourseService courseService;

    @Mock
    EnrollCourseService enrollCourseService;

    @Mock
    ObjectMapper objectMapper;

    private Orders orders;
    private Account account;
    private PaymentDto paymentDto;

    @BeforeEach
    void setUp() {
        //when
        account = new Account();
        account.setUsername("admin");

        orders = new Orders();
        orders.setId(1L);

        paymentDto = new PaymentDto();
    }

    private void setField(Object object, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    @Test
    void createPaymentUrlOrderCartIsEmpty() {
        //given
        List<Long> courseIds = new ArrayList<>();
        paymentDto.setCourseId(courseIds);
        //then
        assertThrows(AppException.class, () ->
                        orderService.createPaymentUrl(paymentDto),
                ErrorCode.ORDER_CART_NULL.getMessage());
    }

    @Test
    void createPaymentUrlOrderCartIsNull() {
        //then
        assertThrows(AppException.class, () ->
                        orderService.createPaymentUrl(paymentDto),
                ErrorCode.ORDER_CART_NULL.getMessage());
    }

    @Test
    void createPaymentUrlSuccess() throws NoSuchFieldException, IllegalAccessException {
        //given
        List<Long> courseIds = new ArrayList<>();
        courseIds.add(1L);
        paymentDto.setCourseId(courseIds);
        List<Course> courses = new ArrayList<>();
        Course newCourse = new Course();
        newCourse.setPrice(100.0);
        courses.add(newCourse);
        setField(orderService, "tmnCode", "9KM2MRF7");
        setField(orderService, "secretKey", "PMRS00LV60QVIJQNIW23P67UXP1RUAFB");
        setField(orderService, "currCode", "VND");
        setField(orderService, "vnpUrl", "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        setField(orderService, "returnUrl", "http://localhost:8080/order/update-status");
        //when
        when(courseService.getCourseByIdsIn(any())).thenReturn(courses);
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(ordersRepo.save(any())).thenReturn(orders);
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(ordersRepo.save(any(Orders.class))).thenReturn(orders);
        //then
        ResponseEntity<String> response = orderService.createPaymentUrl(paymentDto);

        assertNotNull(response);
        assertTrue(Objects.requireNonNull(
                response.getBody()).contains("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html")
        );
    }

    @Test
    void setOrderSuccessSuccess() {
        //given
        List<Long> courseIds = new ArrayList<>();
        courseIds.add(1L);
        courseIds.add(2L);
        double price = 100.0;
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(ordersRepo.save(any(Orders.class))).thenReturn(orders);
        //then
        orderService.setOrder(orders, courseIds, price);

        assertEquals(account.getUsername(), orders.getCreatedBy());
        assertNotNull(orders.getCreatedDate());
        assertEquals(OrderStatus.PENDING, orders.getStatus());
        assertEquals(price, orders.getPrice());
        assertEquals("[1,2]", orders.getOrderCourseJson());
    }

    @Test
    void orderSuccess() {
        //given
        String txnRef = "1";
        String responseCode = "00";
        orders.setOrderCourseJson("[1,2]");
        orders.setStatus(OrderStatus.PENDING);
        orders.setCreatedBy(account.getUsername());
        //when
        when(ordersRepo.findOrdersById(anyLong())).thenReturn(orders);
        when(ordersRepo.save(any(Orders.class))).thenReturn(orders);
        //then
        orderService.orderSuccess(txnRef, responseCode);

        assertEquals(OrderStatus.PAID, orders.getStatus());
        assertEquals(List.of(1L, 2L), orders.getOrderCourse());
    }

    @Test
    void orderSuccessFailResponseCode() {
        //given
        String txnRef = "1";
        String responseCode = "200";
        orders.setStatus(OrderStatus.PENDING);
        //when
        when(ordersRepo.findOrdersById(anyLong())).thenReturn(orders);
        when(ordersRepo.save(any(Orders.class))).thenReturn(orders);
        //then
        Orders result = orderService.orderSuccess(txnRef, responseCode);

        assertEquals(OrderStatus.FAIL, result.getStatus());
    }

    @Test
    void orderSuccessOrderFailReadValue() {
        //given
        String txnRef = "1";
        String responseCode = "00";
        orders.setOrderCourseJson("invalid");
        //when
        when(ordersRepo.findOrdersById(anyLong())).thenReturn(orders);
        //then
        assertThrows(AppException.class,
                () -> orderService.orderSuccess(txnRef, responseCode),
                ErrorCode.ORDER_FAIL.getMessage());
    }
}
