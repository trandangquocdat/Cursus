package com.fpt.cursus.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.cursus.dto.request.PaymentDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.entity.Orders;
import com.fpt.cursus.entity.OrdersDetail;
import com.fpt.cursus.enums.OrderStatus;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.OrdersDetailRepo;
import com.fpt.cursus.repository.OrdersRepo;
import com.fpt.cursus.service.impl.OrderServiceImpl;
import com.fpt.cursus.util.AccountUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import javax.crypto.Mac;
import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @InjectMocks
    private OrderServiceImpl orderService;
    @Mock
    private AccountUtil accountUtil;
    @Mock
    private OrdersRepo ordersRepo;
    @Mock
    private CourseService courseService;
    @Mock
    private EnrollCourseService enrollCourseService;
    @Mock
    private OrdersDetailRepo ordersDetailRepo;
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
        orders.setCreatedBy("admin");

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

    @Test
    void testFindAllByIdInSuccess() {
        //given
        List<Long> ids = new ArrayList<>();
        ids.add(1L);
        List<OrdersDetail> ordersDetails = new ArrayList<>();
        OrdersDetail ordersDetail = new OrdersDetail();
        ordersDetail.setId(1L);
        ordersDetails.add(ordersDetail);
        //when
        when(ordersDetailRepo.findAllByIdIn(ids)).thenReturn(ordersDetails);
        //then
        List<OrdersDetail> result = orderService.findAllByIdIn(ids);

        assertEquals(1, result.size());
    }

    @Test
    void testFindAllByIdInFail() {
        //given
        List<Long> ids = new ArrayList<>();
        ids.add(1L);
        //when
        when(ordersDetailRepo.findAllByIdIn(ids)).thenReturn(new ArrayList<>());
        //then
        assertThrows(AppException.class,
                () -> orderService.findAllByIdIn(ids),
                ErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    void testOrderSuccessSuccess() throws JsonProcessingException {
        //given
        String txnRef = "1";
        String responseCode = "00";
        orders.setOrderCourseJson(List.of("1").toString());
        orders.setOrdersDetails(List.of(new OrdersDetail(), new OrdersDetail()));
        //when
        when(ordersRepo.findOrdersById(anyLong())).thenReturn(orders);
        when(ordersRepo.save(any(Orders.class))).thenReturn(orders);
        //then
        Orders result = orderService.orderSuccess(txnRef, responseCode);

        assertEquals(OrderStatus.PAID, result.getStatus());
        verify(enrollCourseService, times(1)).enrollCourseAfterPay(anyList(), anyString());
    }

    @Test
    void testCreatePaymentUrl() throws NoSuchFieldException, IllegalAccessException {
        //given
        List<Long> courseIds = new ArrayList<>();
        courseIds.add(1L);
        paymentDto.setCourseId(courseIds);
        List<Course> courses = new ArrayList<>();
        Course course = new Course();
        course.setPrice(1000);
        courses.add(course);

        setField(orderService, "tmnCode", "tmnCode");
        setField(orderService, "secretKey", "secretKey");
        setField(orderService, "currCode", "VND");
        setField(orderService, "vnpUrl", "vnpUrl");
        setField(orderService, "returnUrl", "returnUrl");
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(courseService.getCourseByIdsIn(anyList())).thenReturn(courses);
        when(courseService.getCourseById(anyLong())).thenReturn(course);

        //then
        ResponseEntity<String> result = orderService.createPaymentUrl(paymentDto);

        verify(ordersRepo, times(2)).save(any(Orders.class));
        verify(ordersDetailRepo, times(1)).saveAll(anyList());
        assertTrue(result.getStatusCode().is2xxSuccessful());
    }

    @Test
    void testCreatePaymentUrlCourseNotFound() {
        //given
        List<Long> courseIds = new ArrayList<>();
        courseIds.add(1L);
        paymentDto.setCourseId(courseIds);
        //when
        when(courseService.getCourseByIdsIn(anyList())).thenReturn(null);
        //then
        assertThrows(AppException.class,
                () -> orderService.createPaymentUrl(paymentDto),
                ErrorCode.COURSE_NOT_FOUND.getMessage());
    }

    @Test
    void testCreatePaymentUrlORDER_GENERATE_HMAC_FAIL() throws NoSuchFieldException,
            IllegalAccessException {
        //given
        List<Long> courseIds = new ArrayList<>();
        courseIds.add(1L);
        paymentDto.setCourseId(courseIds);
        List<Course> courses = new ArrayList<>();
        Course course = new Course();
        course.setPrice(1000);
        courses.add(course);

        setField(orderService, "tmnCode", "tmnCode");
        setField(orderService, "secretKey", "secretKey");
        setField(orderService, "currCode", "VND");
        setField(orderService, "vnpUrl", "vnpUrl");
        setField(orderService, "returnUrl", "returnUrl");
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(courseService.getCourseByIdsIn(anyList())).thenReturn(courses);
        when(courseService.getCourseById(anyLong())).thenReturn(course);
        try (MockedStatic<Mac> mockedMac = mockStatic(Mac.class)) {
            mockedMac.when(() -> Mac.getInstance(anyString())).thenThrow(new NoSuchAlgorithmException());

            assertThrows(AppException.class,
                    () -> orderService.createPaymentUrl(paymentDto),
                    ErrorCode.ORDER_GENERATE_HMAC_FAIL.getMessage());
        }
    }

}
