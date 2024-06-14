// File: src/main/java/com/fpt/cursus/service/UserService.java
package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.ChangePasswordDto;
import com.fpt.cursus.dto.request.LoginReqDto;
import com.fpt.cursus.dto.request.RegisterReqDto;
import com.fpt.cursus.dto.request.ResetPasswordDto;
import com.fpt.cursus.dto.EnrollCourseDto;
import com.fpt.cursus.dto.response.LoginResDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.entity.Otp;
import com.fpt.cursus.enums.status.UserStatus;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.AccountRepo;
import com.fpt.cursus.repository.CourseRepo;
import com.fpt.cursus.repository.OtpRepo;
import com.fpt.cursus.util.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class UserService {

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private CourseRepo courseRepo;

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
    private EmailUtil emailUtil;

    @Autowired
    private Regex regex;

    @Autowired
    private OtpService otpService;

    @Autowired
    private MapperUtil mapperUtil;

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
        account.setStatus(UserStatus.ACTIVE);
        List<EnrollCourseDto> enrolledCourses = new ArrayList<>();
        EnrollCourseDto course1 = new EnrollCourseDto();
        course1.setCourseName("Course 1");
        EnrollCourseDto course2 = new EnrollCourseDto();
        course2.setCourseName("Course 2");
        enrolledCourses.add(course1);
        enrolledCourses.add(course2);
        MapperUtil mapperUtil = new MapperUtil();
        String enrolledCourseJson = mapperUtil.serializeCourseList(enrolledCourses);
        account.setEnrolledCourseJson(enrolledCourseJson);

        return accountRepo.save(account);
    }

    public LoginResDto login(LoginReqDto loginReqDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginReqDto.getUsername(), loginReqDto.getPassword())
            );

            Account account = (Account) authentication.getPrincipal();

            if (!account.getStatus().equals(UserStatus.ACTIVE)) {
                throw new AppException(ErrorCode.EMAIL_UNAUTHENTICATED);
            }

            LoginResDto loginResDto = new LoginResDto();
            loginResDto.setToken(tokenHandler.generateToken(account));
            loginResDto.setRefreshToken(tokenHandler.generateRefreshToken(account));
            loginResDto.setUsername(account.getUsername());
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
            loginResponseDTO.setUsername(account.getUsername());
            loginResponseDTO.setRefreshToken(tokenHandler.generateRefreshToken(account));
            return loginResponseDTO;
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            System.out.println(e);
        }
        return null;
    }

    public void verifyAccount(String email, String otp) {
        Account account = accountRepo.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Otp userOtp = otpRepo.findMailByEmail(email);
        if (validateOtp(userOtp, otp)) {
            account.setStatus(UserStatus.ACTIVE);
            accountRepo.save(account);
            LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(2);
            otpRepo.deleteOldOtps(email, expiryTime);
        } else {
            throw new AppException(ErrorCode.OTP_INVALID);
        }
    }

    public void regenerateOtp(String email) {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(2);
        otpRepo.deleteOldOtps(email, expiryTime);
        String otp = String.valueOf(otpService.generateOtp());
        otpService.sendOtpEmail(email, otp);
        otpService.saveOtp(email, otp);
    }

    public void deleteAccount(String username) {
        Account account = accountRepo.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

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
        Account account = accountRepo.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Otp userOtp = otpRepo.findMailByEmail(email);
        if (validateOtp(userOtp, otp)) {
            account.setPassword(passwordEncoder.encode(resetPasswordDto.getPassword()));
            accountRepo.save(account);
            LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(2);
            otpRepo.deleteOldOtps(email, expiryTime);
        } else {
            throw new AppException(ErrorCode.OTP_INVALID);
        }
    }

    public void forgotPassword(String email) {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(2);
        otpRepo.deleteOldOtps(email, expiryTime);
        String otp = otpService.generateOtp();
        otpService.sendResetPasswordEmail(email, otp);
        otpService.saveOtp(email, otp);
    }

    private boolean validateOtp(Otp userOtp, String otp) {
        if (Duration.between(userOtp.getOtpGeneratedTime(), LocalDateTime.now()).getSeconds() < (2 * 60)) {
            return userOtp.getOtp().equals(otp);
        } else {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }
    }


    //Detailed Information
    public List<Course> getEnrolledCoursesByUsername(String username) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            Account account_check = (Account) authentication.getPrincipal();
            if (!account_check.getUsername().equals(username)) {
               throw new AppException(ErrorCode.USER_UNAUTHORIZED);
            }else{
                Account account = (Account) this.accountRepo.findByUsername(username).orElseThrow(() -> {
                    throw new AppException(ErrorCode.USER_NOT_FOUND);
                });
                if (account.getEnrolledCourseJson() == null || account.getEnrolledCourseJson().isEmpty()) {
                    throw new AppException(ErrorCode.USER_ENROLLED_EMPTY);
                } else {
                    List<EnrollCourseDto> enrolledCourses = mapperUtil.deserializeCourseList(account.getEnrolledCourseJson(), EnrollCourseDto.class);
                    List<String> courseNames = enrolledCourses.stream().map(EnrollCourseDto::getCourseName).collect(Collectors.toList());
                    List<Course> courses = courseRepo.findByNameIn(courseNames);
                    return courses;
                }
            }
        }else{
                throw new AppException(ErrorCode.UNCATEGORIZED_ERROR);
        }
    }

    //Only Name of course
//    public List<?> getEnrolledCoursesByUsernameNotDetailed(String username) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
//            Account account_check = (Account) authentication.getPrincipal();
//            if (!account_check.getUsername().equals(username)) {
//                throw new AppException(ErrorCode.USER_UNAUTHORIZED);
//            }else{
//                Account account = (Account) this.accountRepo.findByUsername(username).orElseThrow(() -> {
//                    throw new AppException(ErrorCode.USER_NOT_FOUND);
//                });
//                if (account.getEnrolledCourseJson() == null || account.getEnrolledCourseJson().isEmpty()) {
//                    throw new AppException(ErrorCode.USER_ENROLLED_EMPTY);
//                } else {
//                    List<EnrollCourseDto> enrolledCourses = mapperUtil.deserializeCourseList(account.getEnrolledCourseJson(), EnrollCourseDto.class);
//                    List<String> courseNames = enrolledCourses.stream().map(EnrollCourseDto::getCourseName).collect(Collectors.toList());
//                    List<Course> courses = courseRepo.findByNameIn(courseNames);
//                   List<EnrollCourseDto> courseResponse = courses.stream().map(course -> new EnrollCourseDto(
//                           course.getName(),
//                           course.getCategory().toString(),
//                           course.getPrice(),
//                           course.getRating()
//                   )).collect(Collectors.toList());
//                   return courseResponse;
//                }
//            }
//        }else{
//            throw new AppException(ErrorCode.UNCATEGORIZED_ERROR);
//        }
//    }
}

