package com.fpt.cursus.service.impl;

import com.fpt.cursus.dto.request.ChangePasswordDto;
import com.fpt.cursus.dto.request.LoginReqDto;
import com.fpt.cursus.dto.request.RegisterReqDto;
import com.fpt.cursus.dto.request.ResetPasswordDto;
import com.fpt.cursus.dto.response.LoginResDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Otp;
import com.fpt.cursus.enums.InstructorStatus;
import com.fpt.cursus.enums.Role;
import com.fpt.cursus.enums.UserStatus;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.AccountRepo;
import com.fpt.cursus.service.AccountService;
import com.fpt.cursus.service.FileService;
import com.fpt.cursus.service.OtpService;
import com.fpt.cursus.util.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.HttpServletRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepo accountRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenHandler tokenHandler;
    private final AccountUtil accountUtil;
    private final Regex regex;
    private final OtpService otpService;
    private final PageUtil pageUtil;
    private final FileService fileService;
    private final FileUtil fileUtil;
    private final ModelMapper modelMapper;

    @Value("${spring.security.jwt.access-token-expiration}")
    private long accessTokenExpiration;
    @Value("${spring.otp.expiration}")
    private long otpExpiration;

    @Autowired
    public AccountServiceImpl(AccountRepo accountRepo,
                              PasswordEncoder passwordEncoder,
                              AuthenticationManager authenticationManager,
                              TokenHandler tokenHandler,
                              AccountUtil accountUtil,
                              Regex regex,
                              OtpService otpService,
                              PageUtil pageUtil,
                              FileService fileService,
                              FileUtil fileUtil,
                              ModelMapper modelMapper) {
        this.accountRepo = accountRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenHandler = tokenHandler;
        this.accountUtil = accountUtil;
        this.regex = regex;
        this.otpService = otpService;
        this.pageUtil = pageUtil;
        this.fileService = fileService;
        this.fileUtil = fileUtil;
        this.modelMapper = modelMapper;
    }

    @Override
    public Account register(RegisterReqDto registerReqDto) {
        validateRegisterRequest(registerReqDto);

        Account account = modelMapper.map(registerReqDto, Account.class);
        account.setCreatedDate(new Date());
        account.setPassword(passwordEncoder.encode(registerReqDto.getPassword()));
        account.setRole(Role.STUDENT);
        account.setStatus(UserStatus.INACTIVE);

        if (fileUtil.isImage(registerReqDto.getAvatar())) {
            fileService.setAvatar(registerReqDto.getAvatar(), account);
        } else {
            throw new AppException(ErrorCode.FILE_INVALID_IMAGE);
        }

        return accountRepo.save(account);
    }

    private void validateRegisterRequest(RegisterReqDto registerReqDto) {
        if (!regex.isPhoneValid(registerReqDto.getPhone())) {
            throw new AppException(ErrorCode.PHONE_NOT_VALID);
        }
        if (accountRepo.existsByUsername(registerReqDto.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_EXISTS);
        }
        if (accountRepo.existsByEmail(registerReqDto.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTS);
        }
    }

    @Override
    public LoginResDto login(LoginReqDto loginReqDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginReqDto.getUsername(), loginReqDto.getPassword()));
            Account account = (Account) authentication.getPrincipal();

            if (!account.getStatus().equals(UserStatus.ACTIVE)) {
                throw new AppException(ErrorCode.EMAIL_UNAUTHENTICATED);
            }

            return buildLoginResponse(account);
        } catch (BadCredentialsException e) {
            throw new AppException(ErrorCode.PASSWORD_NOT_CORRECT);
        }
    }

    private LoginResDto buildLoginResponse(Account account) {
        LoginResDto loginResDto = new LoginResDto();
        loginResDto.setAccessToken(tokenHandler.generateAccessToken(account));
        loginResDto.setRefreshToken(tokenHandler.generateRefreshToken(account));
        loginResDto.setExpire(accessTokenExpiration);
        return loginResDto;
    }

    @Override
    public LoginResDto refreshToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_NOT_VALID);
        }
        String token = authHeader.substring(7);
        String username = tokenHandler.getInfoByToken(token);
        Account account = accountRepo.findAccountByUsername(username);
        if (account == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        return buildLoginResponse(account);
    }

    @Override
    public LoginResDto loginGoogle(String token) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            String email = decodedToken.getEmail();
            Account account = accountRepo.findAccountByEmail(email);
            return buildLoginResponse(account);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Account authenticateAccount(String email, String otp) {
        Otp userOtp = otpService.findOtpByEmailAndValid(email, true);
        if (validateOtp(userOtp, otp)) {
            Account account = accountRepo.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            account.setStatus(UserStatus.ACTIVE);
            otpService.updateOldOtps(email);
            return accountRepo.save(account);
        } else {
            throw new AppException(ErrorCode.OTP_INVALID);
        }
    }

    @Override
    public Account approveInstructorById(Long id, InstructorStatus status) {
        Account account = accountRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (status.equals(InstructorStatus.REJECTED)) {
            account.setInstructorStatus(InstructorStatus.REJECTED);
        } else if (status.equals(InstructorStatus.APPROVED)) {
            account.setRole(Role.INSTRUCTOR);
            account.setInstructorStatus(InstructorStatus.APPROVED);
        }
        return accountRepo.save(account);
    }

    @Override
    public Account sendCv(MultipartFile file) {
        Account account = accountUtil.getCurrentAccount();
//        try {
//            if (!fileUtil.isPDF(file)) {
//                throw new AppException(ErrorCode.FILE_INVALID_PDF);
//            }
//            fileService.uploadFile(file);
//        } catch (IOException e) {
//            throw new AppException(ErrorCode.FILE_UPLOAD_FAIL);
//        }
//        account.setInstructorStatus(InstructorStatus.WAITING);
        return accountRepo.save(account);
    }

    @Override
    public List<Account> getInstructorByInstStatus(InstructorStatus status) {
        List<Account> accounts = accountRepo.findAccountByInstructorStatus(status);
        if (accounts.isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        return accounts;
    }

    @Override
    public List<Account> getInstructorByName(String name) {
        List<Account> accounts = accountRepo.findByFullNameLikeAndInstructorStatus("%" + name + "%", InstructorStatus.APPROVED);
        if (accounts.isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        return accounts;
    }

    @Override
    public String regenerateOtp(String email) {
        if (accountRepo.findByEmail(email).isEmpty()) {
            throw new AppException(ErrorCode.EMAIL_NOT_FOUND);
        }
        otpService.updateOldOtps(email);
        String otp = otpService.generateOtp();
        otpService.sendOtpEmail(email, otp);
        otpService.saveOtp(email, otp);
        return otp;
    }

    @Override
    public Account setStatusAccount(String username, UserStatus status) {
        Account account = accountRepo.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        account.setStatus(status);
        return accountRepo.save(account);
    }

    @Override
    public Account setAdmin(String username) {
        Account account = accountRepo.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        account.setRole(Role.ADMIN);
        account.setStatus(UserStatus.ACTIVE);
        account.setUpdatedBy(accountUtil.getCurrentAccount().getUsername());
        return accountRepo.save(account);
    }

    @Override
    public void changePassword(ChangePasswordDto changePasswordDto) {
        Account account = accountUtil.getCurrentAccount();
        validateChangePasswordRequest(changePasswordDto, account);

        account.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        accountRepo.save(account);
    }

    private void validateChangePasswordRequest(ChangePasswordDto changePasswordDto, Account account) {
        if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), account.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_CORRECT);
        }

        if (passwordEncoder.matches(changePasswordDto.getNewPassword(), account.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_IS_SAME_CURRENT);
        }

        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmNewPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }
    }

    @Override
    public void resetPassword(String email, String otp, ResetPasswordDto resetPasswordDto) {
        if (!resetPasswordDto.getPassword().equals(resetPasswordDto.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }
        Account account = accountRepo.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Otp userOtp = otpService.findOtpByEmailAndValid(email, true);
        if (validateOtp(userOtp, otp)) {
            account.setPassword(passwordEncoder.encode(resetPasswordDto.getPassword()));
            otpService.updateOldOtps(email);
            accountRepo.save(account);
        } else {
            throw new AppException(ErrorCode.OTP_INVALID);
        }
    }

    @Override
    public void forgotPassword(String email) {
        otpService.updateOldOtps(email);
        if (accountRepo.findByEmail(email).isEmpty()) {
            throw new AppException(ErrorCode.EMAIL_NOT_FOUND);
        }
        String otp = otpService.generateOtp();
        otpService.sendResetPasswordEmail(email, otp);
        otpService.saveOtp(email, otp);
    }

    private boolean validateOtp(Otp userOtp, String otp) {
        if (Duration.between(userOtp.getOtpGeneratedTime(), LocalDateTime.now()).getSeconds() < (otpExpiration)) {
            return userOtp.getOtp().equals(otp);
        } else {
            userOtp.setValid(false);
            otpService.saveOtp(userOtp.getEmail(), userOtp.getOtp());
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }
    }

    @Override
    public Page<Account> getListOfStudentAndInstructor(Role role, int offset, int pageSize, String sortBy) {
        pageUtil.checkOffset(offset);
        Pageable pageable = pageUtil.getPageable(sortBy, offset - 1, pageSize);
        if (role != null) {
            return accountRepo.findAccountByRole(role, pageable);
        } else {
            List<Account> instructorList = accountRepo.findAccountByRole(Role.INSTRUCTOR);
            List<Account> studentList = accountRepo.findAccountByRole(Role.STUDENT);
            studentList.addAll(instructorList);
            return new PageImpl<>(studentList, pageable, studentList.size());
        }
    }

    @Override
    public void saveAccount(Account account) {
        accountRepo.save(account);
    }

    @Override
    public Account getAccountByUsername(String username) {
        return accountRepo.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
