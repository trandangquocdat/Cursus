package com.fpt.cursus.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class PaymentDto {
    private List<Long> courseId;
}
