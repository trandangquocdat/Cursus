package com.fpt.cursus.controller;

import com.fpt.cursus.entity.Account;
import com.fpt.cursus.service.AccountService;
import com.fpt.cursus.util.AccountUtil;
import com.fpt.cursus.util.TokenHandler;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    private final AccountService accountService;

    @Autowired
    public TokenController(TokenHandler tokenHandler,
                           AccountUtil accountUtil,
                           AccountService accountService) {
        this.tokenHandler = tokenHandler;
        this.accountUtil = accountUtil;
        this.accountService = accountService;
    }

    @GetMapping("/token/generate-refresh-token")
    public ResponseEntity<Object> getRefreshToken() {
        Account account = accountUtil.getCurrentAccount();
        return ResponseEntity.ok(tokenHandler.generateRefreshToken(account));
    }

    @PostMapping("/token/refresh-token")
    public ResponseEntity<Object> refreshToken(HttpServletRequest request) {
        return ResponseEntity.ok(accountService.refreshToken(request));
    }
}
