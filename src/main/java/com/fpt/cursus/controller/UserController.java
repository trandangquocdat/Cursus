package com.fpt.cursus.controller;

import com.fpt.cursus.dto.*;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.service.OtpService;
import com.fpt.cursus.service.UserService;
import com.fpt.cursus.util.ApiResUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private OtpService otpService;
    @Autowired
    private ApiResUtil apiResUtil;

    @PostMapping("/register")
    public ApiRes<?>  register(@RequestBody @Valid RegisterReqDto account) {
        Account newAccount = userService.register(account);
        String otp = otpService.generateOtp();
        otpService.sendOtpEmail(account.getEmail(),otp);
        otpService.saveOtp(account.getEmail(),otp);
        String successMessage = "Register successfully. Please check your email to verify your account.";
        return apiResUtil.returnApiRes(true,HttpStatus.OK.value(),successMessage,newAccount);
    }

    @PostMapping("/login")
    public ApiRes<?>  login(@RequestBody @Valid LoginReqDto account) {
        LoginResDto newAccount = userService.login(account);
        String successMessage = "Login successfully.";
        return apiResUtil.returnApiRes(true,HttpStatus.OK.value(),successMessage,newAccount);
    }

    @PatchMapping("/verify-account")
    public ApiRes<?>  verifyAccount(@RequestParam String email, @RequestParam String otp) {
        userService.verifyAccount(email, otp);
        String successMessage = "Verify account successfully. You can now login with your email and password.";
        return apiResUtil.returnApiRes(true,HttpStatus.OK.value(),successMessage,null);
    }
    @PutMapping("/regenerate-otp")
    public ApiRes<?>  regenerateOtp(@RequestParam String email) {
        userService.regenerateOtp(email);
        String successMessage = "Regenerate OTP successfully. Please check your email to verify your account.";
        return apiResUtil.returnApiRes(true,HttpStatus.OK.value(),successMessage,null);
    }
    @DeleteMapping("/delete-account")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiRes<?> deleteAccount(@RequestParam String username) {
        userService.deleteAccount(username);
        String successMessage = "Delete account successfully.";
        return apiResUtil.returnApiRes(true,HttpStatus.OK.value(),successMessage,null);
    }

    @PatchMapping("/change-password")
    public ApiRes<?>  changePassword(@RequestBody @Valid ChangePasswordDto changePasswordDto) {
        userService.changePassword(changePasswordDto);
        String successMessage = "Change password successfully.";
        return apiResUtil.returnApiRes(true,HttpStatus.OK.value(),successMessage,null);
    }

}
