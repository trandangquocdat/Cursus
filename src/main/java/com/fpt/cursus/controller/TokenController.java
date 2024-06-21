package com.fpt.cursus.controller;

import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.service.UserService;
import com.fpt.cursus.util.AccountUtil;
import com.fpt.cursus.util.ApiResUtil;
import com.fpt.cursus.util.TokenHandler;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
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
    @Autowired
    private UserService userService;

    @GetMapping("/token/generate-refresh-token")
    public ApiRes<?> getRefreshToken() {
        Account account = accountUtil.getCurrentAccount();
        return apiResUtil.returnApiRes(null, null, null, tokenHandler.generateRefreshToken(account));
    }

    @PostMapping("/token/refresh-token")
    public ApiRes<?> refreshToken(HttpServletRequest request) {
        return apiResUtil.returnApiRes(null, null, null, userService.refreshToken(request));
    }
}
