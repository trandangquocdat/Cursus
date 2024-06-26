package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.*;
import com.fpt.cursus.dto.response.LoginResDto;
import com.fpt.cursus.entity.Account;

import java.util.List;

public interface UserService {
    Account register(RegisterReqDto registerReqDTO);

    LoginResDto login(LoginReqDto loginReqDto);

    LoginResDto loginGoogle(String token);

    void verifyAccount(String email, String otp);

    void verifyInstructorById(Long id);

    void sendVerifyInstructor(Long id, CvLinkDto cvLinkdto);

    List<Account> getVerifyingInstructor();

    void regenerateOtp(String email);

    void deleteAccount(String username);

    void changePassword(ChangePasswordDto changePasswordDto);

    void resetPassword(String email, String otp, ResetPasswordDto resetPasswordDto);

    void forgotPassword(String email);
}
