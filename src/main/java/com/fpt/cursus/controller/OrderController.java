package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.PaymentDto;
import com.fpt.cursus.service.OrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@Tag(name = "Order Controller")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/order/create-url")
    public ResponseEntity<Object> createUrl(@RequestBody PaymentDto request) {
        return ResponseEntity.ok(orderService.createUrl(request));
    }

    @GetMapping("/order/update-status")
    public ResponseEntity<Object> orderSuccess(@RequestParam("vnp_TxnRef") String txnRef,
                                               @RequestParam("vnp_ResponseCode") String responseCode) {
        return ResponseEntity.ok(orderService.orderSuccess(txnRef, responseCode));
    }
}
