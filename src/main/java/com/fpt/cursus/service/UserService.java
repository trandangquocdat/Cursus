package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.*;
import com.fpt.cursus.dto.response.LoginResDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Otp;
import com.fpt.cursus.enums.type.Role;
import com.fpt.cursus.enums.status.UserStatus;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.AccountRepo;
import com.fpt.cursus.repository.OtpRepo;
import com.fpt.cursus.util.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;


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
        account.setRole(Role.STUDENT);
        account.setPhone(registerReqDTO.getPhone());
        account.setGender(registerReqDTO.getGender());
        //verify cv
        account.setAvatar(registerReqDTO.getAvatar());
        account.setStatus(UserStatus.INACTIVE);
        return accountRepo.save(account);
        //
    }

    public LoginResDto login(LoginReqDto loginReqDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginReqDto.getUsername(), loginReqDto.getPassword()));

            Account account = (Account) authentication.getPrincipal();

            if (!account.getStatus().equals(UserStatus.ACTIVE)) {
                throw new AppException(ErrorCode.EMAIL_UNAUTHENTICATED);
            }

            LoginResDto loginResDto = new LoginResDto();
            loginResDto.setToken(tokenHandler.generateToken(account));
            loginResDto.setRefreshToken(tokenHandler.generateRefreshToken(account));
            return loginResDto;
        } catch (BadCredentialsException e) {
            throw new AppException(ErrorCode.PASSWORD_NOT_CORRECT);
        }
    }

    public LoginResDto loginGoogle(String token) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            String email = decodedToken.getEmail();
            Account account = accountRepo.findAccountByEmail(email);
            LoginResDto loginResponseDTO = new LoginResDto();
            loginResponseDTO.setToken(tokenHandler.generateToken(account));
            loginResponseDTO.setRefreshToken(tokenHandler.generateRefreshToken(account));
            return loginResponseDTO;
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            System.out.println(e);
        }
        return null;
    }

    public void verifyAccount(String email, String otp) {
        Otp userOtp = otpRepo.findOtpByEmailAndValid(email, true);
        if (validateOtp(userOtp, otp)) {
            Account account = accountRepo.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            account.setStatus(UserStatus.ACTIVE);
            otpRepo.updateOldOtps(email);
            accountRepo.save(account);
        } else {
            throw new AppException(ErrorCode.OTP_INVALID);
        }
    }

    public void verifyInstructorById(Long id) {
        Account account = accountRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        account.setRole(Role.INSTRUCTOR);
        accountRepo.save(account);
    }
    public void sendVerifyInstructor(Long id, CvLinkDto cvLinkdto) {
        Account account = accountRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        account.setCvLink(cvLinkdto.getCvLink());
        account.setInstructorVerified(true);
        accountRepo.save(account);
    }
    public List<Account> getVerifyingInstructor() {
        return accountRepo.findAccountByInstructorVerified(true);
    }
    public void regenerateOtp(String email) {
        if(accountRepo.findByEmail(email).isEmpty()){
            throw new AppException(ErrorCode.EMAIL_NOT_FOUND);
        }
        otpRepo.updateOldOtps(email);
        String otp = otpService.generateOtp();
        otpService.sendOtpEmail(email, otp);
        otpService.saveOtp(email, otp);
    }

    public void deleteAccount(String username) {
        Account account = accountRepo.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        account.setStatus(UserStatus.DELETED);
        accountRepo.save(account);
    }

    public void changePassword(ChangePasswordDto changePasswordDto) {
        Account account = accountUtil.getCurrentAccount();
        if (account != null) {
            if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), account.getPassword())) {
                throw new AppException(ErrorCode.PASSWORD_NOT_CORRECT);
            }

            if (passwordEncoder.matches(changePasswordDto.getNewPassword(), account.getPassword())) {
                throw new AppException(ErrorCode.PASSWORD_IS_SAME_CURRENT);
            }

            if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmNewPassword())) {
                throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
            }

            account.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
            accountRepo.save(account);
        } else {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
    }

    public void resetPassword(String email, String otp, ResetPasswordDto resetPasswordDto) {
        if (!resetPasswordDto.getPassword().equals(resetPasswordDto.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }
        Account account = accountRepo.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Otp userOtp = otpRepo.findOtpByEmailAndValid(email, true);
        if (validateOtp(userOtp, otp)) {
            account.setPassword(passwordEncoder.encode(resetPasswordDto.getPassword()));
            otpRepo.updateOldOtps(email);
            accountRepo.save(account);
        } else {
            throw new AppException(ErrorCode.OTP_INVALID);
        }
    }

    public void forgotPassword(String email) {
        if(accountRepo.findByEmail(email).isEmpty()){
            throw new AppException(ErrorCode.EMAIL_NOT_FOUND);
        }
        String otp = otpService.generateOtp();
        otpService.sendResetPasswordEmail(email, otp);
        otpService.saveOtp(email, otp);
    }

    private boolean validateOtp(Otp userOtp, String otp) {
        if (Duration.between(userOtp.getOtpGeneratedTime(), LocalDateTime.now()).getSeconds() < (20)) {
            return userOtp.getOtp().equals(otp);
        } else {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }
    }

}

