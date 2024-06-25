package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.PaymentDto;
import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.service.OrderService;
import com.fpt.cursus.util.ApiResUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class OrderController {
    @Autowired
    private ApiResUtil apiResUtil;
    @Autowired
    private OrderService orderService;

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
