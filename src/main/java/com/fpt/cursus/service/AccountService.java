package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.*;
import com.fpt.cursus.dto.response.LoginResDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.enums.type.InstructorStatus;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface AccountService {
    Account register(RegisterReqDto registerReqDTO);

    LoginResDto login(LoginReqDto loginReqDto);

    LoginResDto refreshToken(HttpServletRequest request);

    LoginResDto loginGoogle(String token);

    void verifyAccount(String email, String otp);

    void verifyInstructorById(Long id, InstructorStatus status);

    void sendVerifyInstructor(CvLinkDto cvLinkdto);

    List<Account> getInstructorByInstStatus(InstructorStatus status);

    void regenerateOtp(String email);

    void deleteAccount(String username);

    void setAdmin(String username);

    void changePassword(ChangePasswordDto changePasswordDto);

    void resetPassword(String email, String otp, ResetPasswordDto resetPasswordDto);

    void forgotPassword(String email);

    void saveAccount(Account account);

    boolean existAdmin(String username);

    Account getAccountByUsername(String username);
}
