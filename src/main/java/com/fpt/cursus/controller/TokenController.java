package com.fpt.cursus.controller;

import com.fpt.cursus.dto.ApiRes;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.util.AccountUtil;
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
    @GetMapping("/refresh-token")
    public ApiRes<String> getRefreshToken() {
        Account account = accountUtil.getCurrentAccount();
        ApiRes<String> apiRes = new ApiRes<>();
        apiRes.setCode(HttpStatus.OK.value());
        apiRes.setStatus(true);
        apiRes.setResult(tokenHandler.generateRefreshToken(account));
        apiRes.setMessage("Get refresh token successfully");
        return apiRes;
    }
    @PutMapping("/refresh-token")
    public ApiRes<String> refreshToken(@RequestParam String refreshToken) {
        ApiRes<String> apiRes = new ApiRes<>();
        apiRes.setCode(HttpStatus.OK.value());
        apiRes.setStatus(true);
        apiRes.setResult(tokenHandler.refreshAccessToken(refreshToken));
        apiRes.setMessage("Refresh token successfully");
        return apiRes;
    }
}
