package com.fpt.cursus.dto.response;

import lombok.Data;

@Data
public class LoginResDto {
    private String token;
    private String refreshToken;
}
