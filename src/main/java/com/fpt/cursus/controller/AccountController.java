package com.fpt.cursus.controller;

import com.fpt.cursus.dto.request.*;
import com.fpt.cursus.dto.response.LoginResDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.service.AccountService;
import com.fpt.cursus.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@Validated
@Tag(name = "Account Controller")
public class AccountController {

    private final AccountService accountService;
    private final OtpService otpService;

    @Autowired
    public AccountController(AccountService accountService,
                             OtpService otpService) {
        this.accountService = accountService;
        this.otpService = otpService;
    }

    @Operation(summary = "Register new account", description = "API Register new account, auto send otp to email")

    @PostMapping(value = "/register", consumes = "multipart/form-data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<Object> register(@Valid @ModelAttribute RegisterReqDto registerReqDto) {
        Account newAccount = accountService.register(registerReqDto);
        String otp = otpService.generateOtp();
        otpService.updateOldOtps(registerReqDto.getEmail());
        otpService.sendOtpEmail(registerReqDto.getEmail(), otp);
        otpService.saveOtp(registerReqDto.getEmail(), otp);
        return ResponseEntity.status(HttpStatus.CREATED).body(newAccount);
    }

    @Operation(summary = "Login")
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody @Valid LoginReqDto loginAccountDto) {
        LoginResDto account = accountService.login(loginAccountDto);
        return ResponseEntity.status(HttpStatus.OK).body(account);
    }

    @PostMapping("/login-google-firebase")
    public ResponseEntity<Object> loginGoogle(@RequestBody LoginGoogleReq loginGoogleReq) {
        LoginResDto account = accountService.loginGoogle(loginGoogleReq.getToken());
        return ResponseEntity.status(HttpStatus.OK).body(account);
    }

    @Operation(summary = "Verify Otp from email register", description = "Run after user click on link in email")
    @GetMapping("/auth/authenticate-account")
    public ResponseEntity<Object> authenticateAccount(@RequestParam String email, @RequestParam String otp) {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.authenticateAccount(email, otp));
    }

    @Operation(summary = "Regenerate Otp for email register")
    @PutMapping("/auth/regenerate-otp")
    public ResponseEntity<Object> regenerateOtp(@RequestParam String email) {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.regenerateOtp(email));
    }

    @PatchMapping("/change-password")
    public ResponseEntity<Object> changePassword(@RequestBody @Valid ChangePasswordDto changePasswordDto) {
        accountService.changePassword(changePasswordDto);
        return ResponseEntity.status(HttpStatus.OK).body("Change password successfully");
    }

    @GetMapping("/auth/forgot-password")
    public ResponseEntity<Object> forgotPassword(@RequestParam String email) {
        accountService.forgotPassword(email);
        return ResponseEntity.status(HttpStatus.OK).body("Check your email to reset password");
    }

    @PutMapping("/auth/reset-password")
    public ResponseEntity<Object> resetPassword(@RequestParam String email, @RequestParam String otp, @RequestBody @Valid ResetPasswordDto resetPasswordDto) {
        accountService.resetPassword(email, otp, resetPasswordDto);
        return ResponseEntity.status(HttpStatus.OK).body("Reset password successfully");
    }

}



