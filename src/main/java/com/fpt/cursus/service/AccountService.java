package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.ChangePasswordDto;
import com.fpt.cursus.dto.request.LoginReqDto;
import com.fpt.cursus.dto.request.RegisterReqDto;
import com.fpt.cursus.dto.request.ResetPasswordDto;
import com.fpt.cursus.dto.response.LoginResDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.enums.InstructorStatus;
import com.fpt.cursus.enums.Role;
import com.fpt.cursus.enums.UserStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AccountService {
    Account register(RegisterReqDto registerReqDTO);

    LoginResDto login(LoginReqDto loginReqDto);

    LoginResDto refreshToken(HttpServletRequest request);

    LoginResDto loginGoogle(String token);

    Account authenticateAccount(String email, String otp);

    Account approveInstructorById(Long id, InstructorStatus status);

    Account sendCv(MultipartFile file);

    Page<Account> getInstructorByInstStatus(InstructorStatus status, int offset, int pageSize, String sortBy);

    Page<Account> getInstructorByName(String name, int offset, int pageSize, String sortBy);

    String regenerateOtp(String email);

    Account setStatusAccount(String username, UserStatus status);

    Account setAdmin(String username);

    void changePassword(ChangePasswordDto changePasswordDto);

    void resetPassword(String email, String otp, ResetPasswordDto resetPasswordDto);

    void forgotPassword(String email);

    Page<Account> getListOfStudentAndInstructor(Role role, int offset, int pageSize, String sortBy);

    void saveAccount(Account account);

    Account getAccountByUsername(String username);

    List<Long> getSubscribersUsers(Account account);

    void subscribeInstructor(Long id);

    void unsubscribeInstructor(Long id);

    Account getAccountByEmail(String email);

    void uploadAvatar(MultipartFile avatar, String folder, Account account);

    Account getProfile();

    Page<Account> getAllInstructor(int offset, int pageSize, String sortBy);

    Page<Account> getSubscribers(int offset, int pageSize, String sortBy);

    Page<Account> getSubscribing(int offset, int pageSize, String sortBy);
}
