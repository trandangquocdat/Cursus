package com.fpt.cursus.controller;

import com.fpt.cursus.dto.LoginReqDto;
import com.fpt.cursus.dto.LoginResDto;
import com.fpt.cursus.dto.RegisterReqDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/authentication/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterReqDto account) {
        Account newAccount = authService.register(account);
        return ResponseEntity.ok("Đăng ký thành công. Vui lòng kiểm tra email để hoàn thành xác nhận tài khoản.");
    }

    @PostMapping("/authentication/login")
    public ResponseEntity<?> login(@RequestBody LoginReqDto account) {
        LoginResDto newAccount = authService.login(account);
        return ResponseEntity.ok(newAccount);
    }

    @PutMapping("/verifyAccount")
    public ResponseEntity<String> verifyAccount(@RequestParam String email,
                                                @RequestParam String otp) {
        return new ResponseEntity<>(authService.verifyAccount(email, otp), HttpStatus.OK);
    }
    @PatchMapping("/regenerateOtp")
    public ResponseEntity<String> regenerateOtp(@RequestParam String email) {
        return new ResponseEntity<>(authService.regenerateOtp(email), HttpStatus.OK);
    }
    @DeleteMapping("/deleteAccount")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteAccount(@RequestParam String email) {
        return new ResponseEntity<>(authService.deleteAccount(email), HttpStatus.OK);
    }


}
