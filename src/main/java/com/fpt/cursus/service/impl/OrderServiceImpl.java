package com.fpt.cursus.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.dto.request.PaymentDto;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.entity.Orders;
import com.fpt.cursus.enums.OrderStatus;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.OrdersRepo;
import com.fpt.cursus.service.CourseService;
import com.fpt.cursus.service.EnrollCourseService;
import com.fpt.cursus.service.OrderService;
import com.fpt.cursus.util.AccountUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class OrderServiceImpl implements OrderService {
    private final AccountUtil accountUtil;
    private final CourseService courseService;
    private final OrdersRepo ordersRepo;
    private final EnrollCourseService enroll;
    @Value("${spring.vnpay.tmnCode}")
    private String tmnCode;
    @Value("${spring.vnpay.secretKey}")
    private String secretKey;
    @Value("${spring.vnpay.currCode}")
    private String currCode;
    @Value("${spring.vnpay.vnpUrl}")
    private String vnpUrl;
    @Value("${spring.vnpay.returnUrl}")
    private String returnUrl;

    @Autowired
    public OrderServiceImpl(AccountUtil accountUtil,
                            CourseService courseService,
                            OrdersRepo ordersRepo,
                            EnrollCourseService enroll) {
        this.accountUtil = accountUtil;
        this.courseService = courseService;
        this.ordersRepo = ordersRepo;
        this.enroll = enroll;
    }

    @Override
    public ResponseEntity<String> createPaymentUrl(PaymentDto request) {
        List<Long> ids = request.getCourseId();
        if (ids == null || ids.isEmpty()) {
            throw new AppException(ErrorCode.ORDER_CART_NULL);
        }
        Orders order = new Orders();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime createDate = LocalDateTime.now();
        String formattedCreateDate = createDate.format(formatter);
        double prices = 0;
        List<Course> courses = courseService.getCourseByIdsIn(ids);
        for (Course course : courses) {
            if (course == null) {
                throw new AppException(ErrorCode.COURSE_NOT_FOUND);
            }
            prices += course.getPrice();
        }
        setOrder(order, ids, prices);

        String id = String.valueOf(order.getId());
        String price = String.valueOf((long) (prices * 100)); // VND amount should be in cents

        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_CurrCode", currCode);
        vnpParams.put("vnp_TxnRef", id);
        vnpParams.put("vnp_OrderInfo", "Payment for: " + id);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Amount", price);
        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_CreateDate", formattedCreateDate);
        vnpParams.put("vnp_IpAddr", "127.0.0.1");

        StringBuilder signDataBuilder = new StringBuilder();
        signUrl(vnpParams, signDataBuilder);
        String signData = signDataBuilder.toString();

        String signed;
        try {
            signed = generateHMAC(secretKey, signData);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new AppException(ErrorCode.ORDER_GENERATE_HMAC_FAIL);
        }

        vnpParams.put("vnp_SecureHash", signed);

        StringBuilder urlBuilder = new StringBuilder(vnpUrl);
        urlBuilder.append("?");
        signUrl(vnpParams, urlBuilder);

        return ResponseEntity.ok(urlBuilder.toString());
    }

    private void signUrl(Map<String, String> vnpParams, StringBuilder signDataBuilder) {
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            signDataBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            signDataBuilder.append("=");
            signDataBuilder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            signDataBuilder.append("&");
        }
        signDataBuilder.deleteCharAt(signDataBuilder.length() - 1); // Remove last '&'
    }

    private String generateHMAC(String secretKey, String signData) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmacSha512 = Mac.getInstance("HmacSHA512");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmacSha512.init(keySpec);
        byte[] hmacBytes = hmacSha512.doFinal(signData.getBytes(StandardCharsets.UTF_8));

        StringBuilder result = new StringBuilder();
        for (byte b : hmacBytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    @Override
    public Orders orderSuccess(String txnRef, String responseCode) {
        Long id = Long.parseLong(txnRef);
        Orders order = ordersRepo.findOrdersById(id);
        if (!responseCode.equals("00")) {
            order.setStatus(OrderStatus.FAIL);
            return ordersRepo.save(order);
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            order.setOrderCourse(mapper.readValue(order.getOrderCourseJson(), new TypeReference<>() {
            }));
            order.setStatus(OrderStatus.PAID);
            String username = order.getCreatedBy();
            enroll.enrollCourseAfterPay(order.getOrderCourse(), username);

        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.ORDER_FAIL);
        }
        return ordersRepo.save(order);
    }
    @Override
    public void setOrder(Orders order, List<Long> ids, double price) {
        order.setCreatedBy(accountUtil.getCurrentAccount().getUsername());
        order.setCreatedDate(new Date());
        order.setStatus(OrderStatus.PENDING);
        order.setPrice(price);
        ObjectMapper mapper = new ObjectMapper();
        try {
            order.setOrderCourseJson(mapper.writeValueAsString(ids));
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.ORDER_FAIL);
        }
        ordersRepo.save(order);
    }
}
