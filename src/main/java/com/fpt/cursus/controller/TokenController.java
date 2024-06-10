package com.fpt.cursus.controller;

import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.util.AccountUtil;
import com.fpt.cursus.util.ApiResUtil;
import com.fpt.cursus.util.TokenHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TokenController {
    @Autowired
    private TokenHandler tokenHandler;
    @Autowired
    private AccountUtil accountUtil;
    @Autowired
    private ApiResUtil apiResUtil;

    @GetMapping("/generate-refresh-token")
    public ApiRes<?> getRefreshToken() {
        Account account = accountUtil.getCurrentAccount();
        String message = "Get refresh token successfully";
        return apiResUtil.returnApiRes(true, HttpStatus.OK.value(), message, tokenHandler.generateRefreshToken(account));
    }

    @PutMapping("/refresh-access-token")
    public ApiRes<?> refreshToken(@RequestParam String refreshToken) {
        String message = "Refresh token successfully";
        return apiResUtil.returnApiRes(true, HttpStatus.OK.value(), message, tokenHandler.refreshAccessToken(refreshToken));
    }
}
