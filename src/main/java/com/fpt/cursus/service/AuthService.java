package com.fpt.cursus.service;

import com.fpt.cursus.dto.LoginReqDto;
import com.fpt.cursus.dto.LoginResDto;
import com.fpt.cursus.dto.RegisterReqDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Mail;
import com.fpt.cursus.enums.AccountStatus;
import com.fpt.cursus.repository.AccountRepo;
import com.fpt.cursus.repository.MailRepo;
import com.fpt.cursus.util.AccountUtil;
import com.fpt.cursus.util.EmailUtil;
import com.fpt.cursus.util.OtpUtil;
import com.fpt.cursus.util.TokenHandler;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class AuthService {
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private MailRepo mailRepo;
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

    public Account register(RegisterReqDto registerReqDTO) {
        // Validate email
        if (!emailUtil.isValidEmail(registerReqDTO.getEmail())) {
            throw new IllegalArgumentException("Email không hợp lệ.");
        }
        // Validate username
        if (accountRepo.existsByUsername(registerReqDTO.getUsername()))
            throw new RuntimeException("Người dùng đã tồn tại trong hệ thống.");
        // Validate email
        if (accountRepo.existsByEmail(registerReqDTO.getEmail()))
            throw new RuntimeException("Email đã tồn tại trong hệ thống.");
        // Send OTP
        String otp = otpUtil.generateOtp();
        try {
            emailUtil.sendOtpEmail(registerReqDTO.getEmail(), otp);
        } catch (Exception e) {
            throw new RuntimeException("Không thể gửi email. Vui lòng thử lại sau.");
        }
        // Create account
        Account account = new Account();
        String rawPassword = registerReqDTO.getPassword();
        account.setUsername(registerReqDTO.getUsername());
        account.setPassword(passwordEncoder.encode(rawPassword));
        account.setEmail(registerReqDTO.getEmail());
        account.setFullName(registerReqDTO.getFullName());
        account.setRole(registerReqDTO.getRole());
        account.setPhone(registerReqDTO.getPhone());
        account.setStatus(AccountStatus.INACTIVE);
        Account newAccount = accountRepo.save(account);
        // Create mail
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
                throw new RuntimeException("Tài khoản chưa xác thực email");

            LoginResDto loginResDto = new LoginResDto();
            loginResDto.setToken(tokenHandler.generateToken(account));
            loginResDto.setUsername(account.getUsername());
            loginResDto.setFullName(account.getFullName());
            loginResDto.setEmail(account.getEmail());
            loginResDto.setPhone(account.getPhone());
            loginResDto.setRole(account.getRole());
            return loginResDto;
        } catch (Exception e) {
            if (e instanceof BadCredentialsException)
                throw new RuntimeException("Mật khẩu không chính xác ");
            else
                throw new InternalAuthenticationServiceException(e.getMessage());
        }
    }

    public String verifyAccount(String email, String otp) {

        Account account = accountRepo.findByEmail(email).orElseThrow(()
                -> new RuntimeException("Không tìm thấy email: " + email));
        Mail userMail = mailRepo.findMailByEmail(email);

        if (Duration.between(userMail.getOtpGeneratedTime(), LocalDateTime.now()).getSeconds() < (1 * 60)) {
            if (userMail.getOtp().equals(otp)) {
                account.setStatus(AccountStatus.ACTIVE);
                accountRepo.save(account);
                return "Xác thực thành công";
            } else {
                return "Mã xác thực không chính xác. Vui lòng thử lại ";
            }
        } else {
            return "Mã xác thực đã hết hiệu lực ";
        }

    }

    public String regenerateOtp(String email) {
        Mail userMail = mailRepo.findByEmail(email).orElseThrow(()
                -> new RuntimeException("Không tìm thấy người dùng có email:  " + email));
        String otp = otpUtil.generateOtp();
        try {
            emailUtil.sendOtpEmail(email, otp);
        } catch (MessagingException e) {
            throw new RuntimeException("Vui lòng thử lại ");
        }
        userMail.setOtp(otp);
        userMail.setOtpGeneratedTime(LocalDateTime.now());
        mailRepo.save(userMail);
        return "Đã gửi mã xác thực. Vui lòng xác thực trong vòng 1 phút.";
    }

    public Account deleteAccount(String username) {
        Account account = accountRepo.findByUsername(username).orElseThrow(()
                -> new RuntimeException("Không tìm thấy người dùng có email: " + username));
        account.setStatus(AccountStatus.DELETED);
        accountRepo.save(account);
        return account;
    }

}
