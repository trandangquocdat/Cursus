package com.fpt.cursus.controller;

import com.fpt.cursus.dto.*;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.service.UserService;
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
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ApiRes<Account> register(@RequestBody @Valid RegisterReqDto account) {
        ApiRes<Account> apiRes = new ApiRes<>();
        Account newAccount = userService.register(account);
        apiRes.setCode(HttpStatus.CREATED.value());
        apiRes.setStatus(true);
        apiRes.setMessage("Register successfully. Please check your email to verify your account");
        apiRes.setResult(newAccount);
        return apiRes;
    }

    @PostMapping("/login")
    public ApiRes<LoginResDto> login(@RequestBody @Valid LoginReqDto account) {
        ApiRes<LoginResDto> apiRes = new ApiRes<>();
        LoginResDto newAccount = userService.login(account);
        apiRes.setCode(HttpStatus.OK.value());
        apiRes.setStatus(true);
        apiRes.setResult(newAccount);
        apiRes.setMessage("Login successfully");
        return apiRes;
    }

    @PatchMapping("/verify-account")
    public ApiRes<?> verifyAccount(@RequestParam String email,
                                                @RequestParam String otp) {
        return userService.verifyAccount(email, otp);
    }
    @PutMapping("/regenerate-otp")
    public ApiRes<?> regenerateOtp(@RequestParam String email) {
        return userService.regenerateOtp(email);
    }
    @DeleteMapping("/delete-account") //
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiRes<?> deleteAccount(@RequestParam String username) {
        return userService.deleteAccount(username);
    }

    @PatchMapping("/change-password")
    public ApiRes<?> changePassword(@RequestBody @Valid ChangePasswordDto changePasswordDto) {
        return userService.changePassword(changePasswordDto);
    }

}
