package com.fpt.cursus.dto.response;

import lombok.Data;

@Data
public class LoginResDto {
    private String accessToken;
    private String refreshToken;
    private long expire;
}
