package com.fpt.cursus.service;

import com.fpt.cursus.dto.LoginReqDto;
import com.fpt.cursus.dto.LoginResDto;
import com.fpt.cursus.dto.RegisterReqDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Mail;
import com.fpt.cursus.enums.AccountStatus;
import com.fpt.cursus.exception.exceptions.AuthenticationException;
import com.fpt.cursus.repository.AccountRepo;
import com.fpt.cursus.repository.MailRepo;
import com.fpt.cursus.util.AccountUtil;
import com.fpt.cursus.util.EmailUtil;
import com.fpt.cursus.util.OtpUtil;
import com.fpt.cursus.util.TokenHandler;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class AuthenticationService {
    @Autowired
    AccountRepo accountRepo;
    @Autowired
    MailRepo mailRepo;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    TokenHandler tokenHandler;
    @Autowired
    AccountUtil accountUtil;
    @Autowired
    OtpUtil otpUtil;
    @Autowired
    EmailUtil emailUtil;
    public Account register(RegisterReqDto registerReqDTO) {
        String otp = otpUtil.generateOtp();
        try {
            emailUtil.sendOtpEmail(registerReqDTO.getEmail(), otp);
        } catch (MessagingException e) {
            throw new RuntimeException("Unable to send email. Please try again later.", e);
        }
        Account account = new Account();
        String rawPassword = registerReqDTO.getPassword();
        account.setUsername(registerReqDTO.getUsername());
        account.setPassword(passwordEncoder.encode(rawPassword));
        account.setEmail(registerReqDTO.getEmail());
        account.setFullName(registerReqDTO.getFullName());
        account.setAvatar(registerReqDTO.getAvatar());
        account.setRole(registerReqDTO.getRole());
        account.setPhone(registerReqDTO.getPhone());
        account.setStatus(AccountStatus.INACTIVE);
        Account newAccount = accountRepo.save(account);

        Mail accountMail = new Mail();

        accountMail.setEmail(registerReqDTO.getEmail());
        accountMail.setOtp(otp);
        accountMail.setOtpGeneratedTime(LocalDateTime.now());
        mailRepo.save(accountMail);
        return newAccount;
    }
    public LoginResDto login(LoginReqDto loginReqDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginReqDto.getUsername(), loginReqDto.getPassword())
            );

            Account account = (Account) authentication.getPrincipal();

            if (!account.getStatus().equals(AccountStatus.ACTIVE))
                throw new MessagingException("Your account is not verified");

            LoginResDto loginResDto = new LoginResDto();
            loginResDto.setToken(tokenHandler.generateToken(account));
            loginResDto.setUsername(account.getUsername());
            loginResDto.setFullName(account.getFullName());
            loginResDto.setEmail(account.getEmail());
            loginResDto.setPhone(account.getPhone());
            loginResDto.setRole(account.getRole());
            return loginResDto;
        } catch (Exception e) {
            throw new InternalAuthenticationServiceException("Authentication failed: " + e.getMessage());
        }
    }

    public String verifyAccount(String email, String otp) {
        Account account = accountRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found with this email: " + email));
        Mail userMail = mailRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found with this email: " + email));
        if (userMail.getOtp().equals(otp) && Duration.between(userMail.getOtpGeneratedTime(),
                LocalDateTime.now()).getSeconds() < (5 * 60)) {
            account.setStatus(AccountStatus.ACTIVE);
            accountRepo.save(account);
            return "OTP verified you can login";
        }
        return "Please regenerate otp and try again";
    }

    public String regenerateOtp(String email) {
        Mail userMail = mailRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found with this email: " + email));
        String otp = otpUtil.generateOtp();
        try {
            emailUtil.sendOtpEmail(email, otp);
        } catch (MessagingException e) {
            throw new RuntimeException("Unable to send otp please try again");
        }
        userMail.setOtp(otp);
        userMail.setOtpGeneratedTime(LocalDateTime.now());
        mailRepo.save(userMail);
        return "Email sent... please verify account within 1 minute";
    }

}
