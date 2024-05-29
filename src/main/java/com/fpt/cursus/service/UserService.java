package com.fpt.cursus.service;

import com.fpt.cursus.dto.ChangePasswordDto;
import com.fpt.cursus.dto.LoginReqDto;
import com.fpt.cursus.dto.LoginResDto;
import com.fpt.cursus.dto.RegisterReqDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Email;
import com.fpt.cursus.enums.UserStatus;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.AccountRepo;
import com.fpt.cursus.repository.EmailRepo;
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
public class UserService {
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private EmailRepo emailRepo;
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
            throw new AppException(ErrorCode.EMAIL_INVALID);
        }
        // Validate username
        if (accountRepo.existsByUsername(registerReqDTO.getUsername()))
            throw new AppException(ErrorCode.USER_EXISTS);
        // Validate email
        if (accountRepo.existsByEmail(registerReqDTO.getEmail()))
            throw new AppException(ErrorCode.EMAIL_EXISTS);
        // Send OTP
        String otp = otpUtil.generateOtp();
        try {
            emailUtil.sendOtpEmail(registerReqDTO.getEmail(), otp); // chay rieng // dat ten email khac
        } catch (Exception e) {
            throw new AppException(ErrorCode.EMAIL_CAN_NOT_SEND);
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
        account.setStatus(UserStatus.INACTIVE);
        Account newAccount = accountRepo.save(account); // try catch
        // Create mail
        Email accountEmail = new Email();
        accountEmail.setEmail(registerReqDTO.getEmail());
        accountEmail.setOtp(otp);
        accountEmail.setOtpGeneratedTime(LocalDateTime.now());
        emailRepo.save(accountEmail);
        return newAccount;
    }

    public LoginResDto login(LoginReqDto loginReqDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginReqDto.getUsername(), loginReqDto.getPassword())
            );

            Account account = (Account) authentication.getPrincipal();

            if (!account.getStatus().equals(UserStatus.ACTIVE))
                throw new AppException(ErrorCode.EMAIL_UNAUTHENTICATED);

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
                throw new AppException(ErrorCode.PASSWORD_NOT_CORRECT);
            else
                throw new AppException(ErrorCode.USER_EXISTS);
        }
    }

    public String verifyAccount(String email, String otp) {

        Account account = accountRepo.findByEmail(email).orElseThrow(()
                -> new AppException(ErrorCode.USER_NOT_FOUND));
        Email userEmail = emailRepo.findMailByEmail(email);

        if (Duration.between(userEmail.getOtpGeneratedTime(), LocalDateTime.now()).getSeconds() < (1 * 24 * 60 * 60)) {
            if (userEmail.getOtp().equals(otp)) {
                account.setStatus(UserStatus.ACTIVE);
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
        Email userEmail = emailRepo.findByEmail(email).orElseThrow(()
                -> new RuntimeException("Không tìm thấy người dùng có email:  " + email));
        String otp = otpUtil.generateOtp();
        try {
            emailUtil.sendOtpEmail(email, otp);
        } catch (MessagingException e) {
            throw new RuntimeException("Vui lòng thử lại ");
        }
        userEmail.setOtp(otp);
        userEmail.setOtpGeneratedTime(LocalDateTime.now());
        emailRepo.save(userEmail);
        return "Đã gửi mã xác thực. Vui lòng xác thực trong vòng 1 phút.";
    }

    public String deleteAccount(String username) {
        Account account = accountRepo.findByUsername(username).orElseThrow(()
                -> new RuntimeException("Không tìm thấy người dùng có email: " + username));
        account.setStatus(UserStatus.DELETED);
        accountRepo.save(account);
        return "Deleted successfully";
    }
    public String changePassword(ChangePasswordDto changePasswordDto) {
        Account account = accountUtil.getCurrentAccount();
        if (account != null) {
            if (!passwordEncoder.matches(changePasswordDto.getOldPassword(), account.getPassword())) {
                return "Mật khẩu không chính xác ";
            }

            // Step 2: Check that new password is different from current password
            if (passwordEncoder.matches(changePasswordDto.getNewPassword(), account.getPassword())) {
                return "Mật khẩu mới không được trùng với mật khẩu hiện tại. ";
            }

            // Step 3: Confirm that new password matches confirmation password
            if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmNewPassword())) {
                return "Mật khẩu không trùng khớp";
            }
            // Update password
            account.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
            accountRepo.save(account);

            return "Mật khẩu đã thay đổi";
        }
        return "Không tìm thấy người dùng";
    }


}
