package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.PaymentDto;
import com.fpt.cursus.entity.Orders;
import com.fpt.cursus.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        OrderService.class
})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new OrderController(orderService)).build();
    }

    @Test
    void createUrl_success() throws Exception {
        when(orderService.createPaymentUrl(any(PaymentDto.class))).thenReturn(ResponseEntity.ok("url"));

        mockMvc.perform(post("/order/create-url")
                        .contentType("application/json")
                        .content("{\"someField\":\"someValue\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body", is("url")));
    }

    @Test
    void orderSuccess_success() throws Exception {
        Orders order = new Orders();
        when(orderService.orderSuccess("123", "00")).thenReturn(order);

        mockMvc.perform(get("/order/update-status")
                        .param("vnp_TxnRef", "123")
                        .param("vnp_ResponseCode", "00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }
}
