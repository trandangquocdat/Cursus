package com.fpt.cursus.controller;

import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.util.AccountUtil;
import com.fpt.cursus.util.ApiResUtil;
import com.fpt.cursus.util.TokenHandler;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class TokenController {
    @Autowired
    private TokenHandler tokenHandler;
    @Autowired
    private AccountUtil accountUtil;
    @Autowired
    private ApiResUtil apiResUtil;

    @GetMapping("/token/generate-refresh-token")
    public ApiRes<?> getRefreshToken() {
        Account account = accountUtil.getCurrentAccount();
        return apiResUtil.returnApiRes(null, null, null, tokenHandler.generateRefreshToken(account));
    }

    @PutMapping("/token/refresh-access-token")
    public ApiRes<?> refreshToken(@RequestParam String refreshToken) {
        return apiResUtil.returnApiRes(null, null, null, tokenHandler.refreshAccessToken(refreshToken));
    }
}
