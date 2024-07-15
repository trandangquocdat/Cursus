package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.PaymentDto;
import com.fpt.cursus.entity.Orders;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface OrderService {
    ResponseEntity<String> createPaymentUrl(PaymentDto request);

    Orders orderSuccess(String txnRef, String responseCode);

    void setOrder(Orders order, List<Long> ids, double price);
}
