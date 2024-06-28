package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.PaymentDto;
import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.service.OrderService;
import com.fpt.cursus.util.ApiResUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@Tag(name = "Order Controller")
public class OrderController {
    private final ApiResUtil apiResUtil;
    private final OrderService orderService;

    public OrderController(ApiResUtil apiResUtil, OrderService orderService) {
        this.apiResUtil = apiResUtil;
        this.orderService = orderService;
    }

    @PostMapping("/order/create-url")
    public ApiRes<?> createUrl(@RequestBody PaymentDto request)  {
        return apiResUtil.returnApiRes(null, null, null, orderService.createUrl(request));
    }
    @GetMapping("/order/update-status")
    public ApiRes<?> orderSuccess(@RequestParam("vnp_TxnRef") String txnRef,
                                  @RequestParam("vnp_ResponseCode") String responseCode) {
        orderService.orderSuccess(txnRef, responseCode);
        String successMessage = "Order success";
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }
}
