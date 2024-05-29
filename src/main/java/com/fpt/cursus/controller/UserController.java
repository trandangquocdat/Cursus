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
        apiRes.setMessage("Đăng ký thành công. Vui lòng kiểm tra email để hoàn thành xác nhận tài khoản.");
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
        apiRes.setMessage("Đăng nhập thành công");
        return apiRes;
    }

    @PutMapping("/verifyAccount")
    public ApiRes<String> verifyAccount(@RequestParam String email,
                                                @RequestParam String otp) {
        ApiRes<String> apiRes = new ApiRes<>();
        apiRes.setStatus(true);
        apiRes.setCode(HttpStatus.OK.value());
        apiRes.setMessage(userService.verifyAccount(email, otp));
        return apiRes;
    }
    @PatchMapping("/regenerateOtp")
    public ApiRes<String> regenerateOtp(@RequestParam String email) {
        ApiRes<String> apiRes = new ApiRes<>();
        apiRes.setStatus(true);
        apiRes.setCode(HttpStatus.OK.value());
        apiRes.setMessage(userService.regenerateOtp(email));
        return apiRes;
    }
    @DeleteMapping("/delete-account") //
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiRes<String> deleteAccount(@RequestParam String email) {
        ApiRes<String> apiRes = new ApiRes<>();
        apiRes.setStatus(true);
        apiRes.setCode(HttpStatus.OK.value());
        apiRes.setMessage(userService.deleteAccount(email));
        return apiRes;
    }

    @PatchMapping("/changePassword")
    public ApiRes<String> changePassword(@RequestBody @Valid ChangePasswordDto changePasswordDto) {
        ApiRes<String> apiRes = new ApiRes<>();
        apiRes.setStatus(true);
        apiRes.setCode(HttpStatus.OK.value());
        apiRes.setMessage(userService.changePassword(changePasswordDto));
        return apiRes;
    }

}
