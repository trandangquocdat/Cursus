package com.fpt.cursus.service;

import com.fpt.cursus.dto.*;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Otp;
import com.fpt.cursus.enums.UserStatus;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.AccountRepo;
import com.fpt.cursus.repository.OtpRepo;
import com.fpt.cursus.util.*;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
public class UserService {
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private OtpRepo otpRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenHandler tokenHandler;
    @Autowired
    private AccountUtil accountUtil;
    @Autowired
    private OtpUtil otpUtil;
    @Autowired
    private EmailUtil emailUtil;
    @Autowired
    private Regex regex;
    @Autowired
    private OtpService otpService;


    public Account register(RegisterReqDto registerReqDTO) {
        if (!regex.isPhoneValid(registerReqDTO.getPhone())) {
            throw new AppException(ErrorCode.PHONE_NOT_VALID);
        }
        Account account = new Account();
        account.setUsername(registerReqDTO.getUsername());
        account.setPassword(passwordEncoder.encode(registerReqDTO.getPassword()));
        account.setEmail(registerReqDTO.getEmail());
        account.setFullName(registerReqDTO.getFullName());
        account.setRole(registerReqDTO.getRole());
        account.setPhone(registerReqDTO.getPhone());
        account.setStatus(UserStatus.INACTIVE);
        return accountRepo.save(account);
    }



    public LoginResDto login(LoginReqDto loginReqDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginReqDto.getUsername(), loginReqDto.getPassword()));

            Account account = (Account) authentication.getPrincipal();

            if (!account.getStatus().equals(UserStatus.ACTIVE)) throw new AppException(ErrorCode.EMAIL_UNAUTHENTICATED);

            LoginResDto loginResDto = new LoginResDto();
            loginResDto.setToken(tokenHandler.generateToken(account));
            loginResDto.setRefreshToken(tokenHandler.generateRefreshToken(account));
            loginResDto.setUsername(account.getUsername());
            return loginResDto;
        } catch (BadCredentialsException e) {
            throw new AppException(ErrorCode.PASSWORD_NOT_CORRECT);
        }
    }

    public void verifyAccount(String email, String otp) {

        Account account = accountRepo.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Otp userOtp = otpRepo.findMailByEmail(email);
        if (Duration.between(userOtp.getOtpGeneratedTime(), LocalDateTime.now()).getSeconds() < (2 * 60)) {
            if (userOtp.getOtp().equals(otp)) {
                account.setStatus(UserStatus.ACTIVE);
                accountRepo.save(account);
                LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(2);
                otpRepo.deleteOldOtps(email, expiryTime);
            } else {
                throw new AppException(ErrorCode.OTP_INVALID);
            }
        } else {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }
    }

    public void regenerateOtp(String email) {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(2);
        otpRepo.deleteOldOtps(email, expiryTime);
        String otp = String.valueOf(otpService.generateOtp());
        try {
            emailUtil.sendOtpEmail(email, otp);
            otpService.saveOtp(email, otp);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.EMAIL_CAN_NOT_SEND);
        }
    }

    public void deleteAccount(String username) {
        Account account = accountRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("Account not found with email: " + username));
        account.setStatus(UserStatus.DELETED);
        accountRepo.save(account);
    }

    public void changePassword(ChangePasswordDto changePasswordDto) {
        Account account = accountUtil.getCurrentAccount();
        if (account != null) {
            if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), account.getPassword())) {
                throw new AppException(ErrorCode.PASSWORD_NOT_CORRECT);
            }

            // Step 2: Check that new password is different from current password
            if (passwordEncoder.matches(changePasswordDto.getNewPassword(), account.getPassword())) {
                throw new AppException(ErrorCode.PASSWORD_IS_SAME_CURRENT);
            }

            // Step 3: Confirm that new password matches confirmation password
            if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmNewPassword())) {
                throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
            }
            // Update password
            account.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
            accountRepo.save(account);
        }
        throw new AppException(ErrorCode.USER_NOT_FOUND);
    }

    public void resetPassword(String email, String otp) {
        Account account = accountRepo.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Otp userOtp = otpRepo.findMailByEmail(email);
        if (Duration.between(userOtp.getOtpGeneratedTime(), LocalDateTime.now()).getSeconds() < (2 * 60)) {
            if (userOtp.getOtp().equals(otp)) {
                otpService.sendResetPasswordEmail(email, otp);
                LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(2);
                otpRepo.deleteOldOtps(email, expiryTime);
            } else {
                throw new AppException(ErrorCode.OTP_INVALID);
            }
        } else {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }
    }

}
