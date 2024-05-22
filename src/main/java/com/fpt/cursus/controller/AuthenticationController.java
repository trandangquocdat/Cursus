package com.fpt.cursus.controller;

import com.fpt.cursus.dto.LoginReqDto;
import com.fpt.cursus.dto.LoginResDto;
import com.fpt.cursus.dto.RegisterReqDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.service.AuthenticationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/authentication/register")
    public ResponseEntity<?> register(@RequestBody RegisterReqDto account) {
        Account newAcoount = authenticationService.register(account);
        return ResponseEntity.ok(newAcoount);
    }

    @PostMapping("/authentication/login")
    public ResponseEntity<?> login(@RequestBody LoginReqDto account) {
        LoginResDto newAcoount = authenticationService.login(account);
        return ResponseEntity.ok(newAcoount);
    }

    @PutMapping("/verifyAccount")
    public ResponseEntity<String> verifyAccount(@RequestParam String email,
                                                @RequestParam String otp) {
        return new ResponseEntity<>(authenticationService.verifyAccount(email, otp), HttpStatus.OK);
    }
    @GetMapping("/regenerateOtp")
    public ResponseEntity<String> regenerateOtp(@RequestParam String email) {
        return new ResponseEntity<>(authenticationService.regenerateOtp(email), HttpStatus.OK);
    }


}
