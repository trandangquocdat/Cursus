package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.*;
import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.dto.response.LoginResDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.service.OtpService;
import com.fpt.cursus.service.UserService;
import com.fpt.cursus.service.impl.OtpServiceImpl;
import com.fpt.cursus.service.impl.UserServiceImpl;
import com.fpt.cursus.util.ApiResUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@Validated
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private OtpService otpService;
    @Autowired
    private ApiResUtil apiResUtil;

    @PostMapping("/auth/register")
    public ApiRes<?> register(@Valid @RequestBody RegisterReqDto account) {
        Account newAccount = userService.register(account);
        String otp = otpService.generateOtp();
        otpService.sendOtpEmail(account.getEmail(), otp);
        otpService.saveOtp(account.getEmail(), otp);
        return apiResUtil.returnApiRes(null, null, null, newAccount);
    }

    @PostMapping("/auth/login")
    public ApiRes<?> login(@RequestBody @Valid LoginReqDto account) {
        LoginResDto newAccount = userService.login(account);
        return apiResUtil.returnApiRes(null, null, null, newAccount);
    }

    @PostMapping("/auth/login-google-firebase")
    public ApiRes<?> loginGoogle(@RequestBody LoginGoogleReq loginGoogleReq) {
        LoginResDto newAccount = userService.loginGoogle(loginGoogleReq.getToken());
        return apiResUtil.returnApiRes(null, null, null, newAccount);
    }

    @PatchMapping("/auth/verify-account")
    public ApiRes<?> verifyAccount(@RequestParam String email, @RequestParam String otp) {
        userService.verifyAccount(email, otp);
        String successMessage = "Verify account successfully. You can now login with your email and password.";
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }

    @PatchMapping("/auth/send-verify-instructor")
    public ApiRes<?> verifyInstructor(@RequestParam long id, @RequestBody @Valid CvLinkDto cvLink) {
        userService.sendVerifyInstructor(id, cvLink);
        String successMessage = "Verify instructor successfully. You can now login with your email and password.";
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }

    @PutMapping("/auth/regenerate-otp")
    public ApiRes<?> regenerateOtp(@RequestParam String email) {
        userService.regenerateOtp(email);
        String successMessage = "Regenerate OTP successfully. Please check your email to verify your account.";
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }


    @PatchMapping("/auth/change-password")
    public ApiRes<?> changePassword(@RequestBody @Valid ChangePasswordDto changePasswordDto) {
        userService.changePassword(changePasswordDto);
        String successMessage = "Change password successfully.";
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }

    @GetMapping("/auth/forgot-password")
    public ApiRes<?> forgotPassword(@RequestParam String email) {
        userService.forgotPassword(email);
        String successMessage = "Please check your email to reset your password.";
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }

    @PutMapping("/auth/reset-password")
    public ApiRes<?> resetPassword(@RequestParam String email, @RequestParam String otp, @RequestBody @Valid ResetPasswordDto resetPasswordDto) {
        userService.resetPassword(email, otp, resetPasswordDto);
        String successMessage = "Reset password successfully. Please login with your new password.";
        return apiResUtil.returnApiRes(null, null, successMessage, null);
    }


}



