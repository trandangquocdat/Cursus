package com.fpt.cursus.controller;

import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.service.AccountService;
import com.fpt.cursus.util.AccountUtil;
import com.fpt.cursus.util.ApiResUtil;
import com.fpt.cursus.util.TokenHandler;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@Tag(name = "Token Controller")
public class TokenController {
    private final TokenHandler tokenHandler;
    private final AccountUtil accountUtil;
    private final ApiResUtil apiResUtil;
    private final AccountService accountService;

    public TokenController(TokenHandler tokenHandler,
                           AccountUtil accountUtil,
                           ApiResUtil apiResUtil,
                           AccountService accountService) {
        this.tokenHandler = tokenHandler;
        this.accountUtil = accountUtil;
        this.apiResUtil = apiResUtil;
        this.accountService = accountService;
    }

    @GetMapping("/token/generate-refresh-token")
    public ApiRes<?> getRefreshToken() {
        Account account = accountUtil.getCurrentAccount();
        return apiResUtil.returnApiRes(null, null, null, tokenHandler.generateRefreshToken(account));
    }

    @PostMapping("/token/refresh-token")
    public ApiRes<?> refreshToken(HttpServletRequest request) {
        return apiResUtil.returnApiRes(null, null, null, accountService.refreshToken(request));
    }
}
