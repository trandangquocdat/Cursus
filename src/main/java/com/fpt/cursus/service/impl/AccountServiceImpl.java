package com.fpt.cursus.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.modelmapper.convention.MatchingStrategies;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final ObjectMapper objectMapper;

    @Value("${spring.security.jwt.access-token-expiration}")
    private long accessTokenExpiration;
    @Value("${spring.otp.expiration}")
    private long otpExpiration;

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
                              ModelMapper modelMapper,
                              ObjectMapper objectMapper) {
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
        this.objectMapper = objectMapper;
    }

    @Override
    public Account register(RegisterReqDto registerReqDto) {
        //validate
        validateRegisterRequest(registerReqDto);
        //create account by model mapper
        ModelMapper customModelMapper = new ModelMapper();
        customModelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT).setSkipNullEnabled(true);
        Account account = modelMapper.map(registerReqDto, Account.class);
        account.setCreatedDate(new Date());
        account.setPassword(passwordEncoder.encode(registerReqDto.getPassword()));
        account.setRole(Role.STUDENT);
        account.setStatus(UserStatus.INACTIVE);
        return accountRepo.save(account);
    }

    @Override
    public void uploadAvatar(MultipartFile avatar, String folder, Account account) {
        if (avatar == null) {
            account.setAvatar("defaultAvatar.jpg");
            accountRepo.save(account);
        } else if (fileUtil.isImage(avatar)) {
            String avatarPath = fileService.linkSave(avatar, folder);
            account.setAvatar(avatarPath);
            accountRepo.save(account);
        } else {
            throw new AppException(ErrorCode.FILE_INVALID_IMAGE);
        }
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
            if (account.getStatus().equals(UserStatus.BLOCKED)) {
                throw new AppException(ErrorCode.USER_HAS_BANNED);
            } else if (!account.getStatus().equals(UserStatus.ACTIVE)) {
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
        String folder = account.getUsername();
        if (!fileUtil.isPDF(file)) {
            throw new AppException(ErrorCode.FILE_INVALID_PDF);
        }
        String link = fileService.linkSave(file, folder);
        account.setCvLink(link);
        account.setInstructorStatus(InstructorStatus.WAITING);
        return accountRepo.save(account);
    }

    @Override
    public void subscribeInstructor(Long id) {
        Account account = accountRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Account currentAccount = accountUtil.getCurrentAccount();

        // save subscriber
        List<Long> listSubscriber = getSubscribersUsers(account);
        if (!listSubscriber.contains(currentAccount.getId())) {
            listSubscriber.add(currentAccount.getId());
            saveSubscribersUsers(account, listSubscriber);
            accountRepo.save(account);

            // save subscribing
            List<Long> listSubscribing = getSubscribingUsers(currentAccount);
            listSubscribing.add(id);
            saveSubscribingUsers(currentAccount, listSubscribing);
            accountRepo.save(currentAccount);
        }
    }

    @Override
    public void unsubscribeInstructor(Long id) {
        Account account = accountRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Account currentAccount = accountUtil.getCurrentAccount();

        List<Long> listSubscriber = getSubscribersUsers(account);
        listSubscriber.remove(currentAccount.getId());
        saveSubscribersUsers(account, listSubscriber);
        accountRepo.save(account);

        // save subscribing
        List<Long> listSubscribing = getSubscribingUsers(currentAccount);
        listSubscribing.remove(id);
        saveSubscribingUsers(currentAccount, listSubscribing);
        accountRepo.save(currentAccount);
    }

    @Override
    public Page<Account> getInstructorByInstStatus(InstructorStatus status, int offset, int pageSize, String sortBy) {
        pageUtil.checkOffset(offset);
        Pageable pageable = pageUtil.getPageable(sortBy, offset - 1, pageSize);
        Page<Account> accountsPage = accountRepo.findAccountByInstructorStatus(status, pageable);
        List<Account> accountsWithAvatars = setAvatar(accountsPage.getContent());
        return new PageImpl<>(accountsWithAvatars, pageable, accountsPage.getTotalElements());
    }

    @Override
    public Page<Account> getInstructorByName(String name, int offset, int pageSize, String sortBy) {
        pageUtil.checkOffset(offset);
        Pageable pageable = pageUtil.getPageable(sortBy, offset - 1, pageSize);
        Page<Account> accounts = accountRepo.findByFullNameLikeAndInstructorStatus("%" + name + "%", InstructorStatus.APPROVED, pageable);
        List<Account> accountsWithAvatars = setAvatar(accounts.getContent());
        return new PageImpl<>(accountsWithAvatars, pageable, accounts.getTotalElements());
    }

    @Override
    public Page<Account> getAllInstructor(int offset, int pageSize, String sortBy) {
        pageUtil.checkOffset(offset);
        Pageable pageable = pageUtil.getPageable(sortBy, offset - 1, pageSize);
        Page<Account> accountsPage = accountRepo.findAccountByInstructorStatus(InstructorStatus.APPROVED, pageable);
        List<Account> accountsWithAvatars = setAvatar(accountsPage.getContent());
        return new PageImpl<>(accountsWithAvatars, pageable, accountsPage.getTotalElements());
    }

    public List<Account> setAvatar(List<Account> accounts) {
        if (accounts.isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        } else {
            for (Account account : accounts) {
                if (account.getAvatar() != null) {
                    account.setAvatar(fileService.getSignedImageUrl(account.getAvatar()));
                }
            }
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
            return accountRepo.findAccountByRoleIn(List.of(Role.STUDENT, Role.INSTRUCTOR), pageable);
        }
    }

    @Override
    public Account getProfile() {
        Account account = accountUtil.getCurrentAccount();
        if (account.getAvatar() != null) {
            account.setAvatar(fileService.getSignedImageUrl(account.getAvatar()));
        }
        return account;
    }

    @Override
    public void saveAccount(Account account) {
        accountRepo.save(account);
    }

    @Override
    public Account getAccountByUsername(String username) {
        return accountRepo.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public Account getAccountByEmail(String email) {
        Account account = accountRepo.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (account.getAvatar() != null) {
            account.setAvatar(fileService.getSignedImageUrl(account.getAvatar()));
        }
        return account;
    }

    @Override
    public Page<Account> getSubscribers(int offset, int pageSize, String sortBy) {
        pageUtil.checkOffset(offset);

        // Convert offset to page number for Pageable
        Pageable pageable = pageUtil.getPageable(sortBy, offset - 1, pageSize);

        // Get the current account
        Account account = accountUtil.getCurrentAccount();
        List<Long> subscribers = getSubscribersUsers(account);

        // Fetch paginated subscribers

        Page<Account> accountsPage = accountRepo.findByIdIn(subscribers, pageable);
        List<Account> accountsWithAvatars = setAvatar(accountsPage.getContent());
        return new PageImpl<>(accountsWithAvatars, pageable, accountsPage.getTotalElements());
    }

    @Override
    public Page<Account> getSubscribing(int offset, int pageSize, String sortBy) {
        pageUtil.checkOffset(offset);

        // Convert offset to page number for Pageable
        Pageable pageable = pageUtil.getPageable(sortBy, offset - 1, pageSize);

        // Get the current account
        Account account = accountUtil.getCurrentAccount();
        List<Long> subscribing = getSubscribingUsers(account);

        // Fetch paginated subscribers
        Page<Account> accountsPage = accountRepo.findByIdIn(subscribing, pageable);
        List<Account> accountsWithAvatars = setAvatar(accountsPage.getContent());
        return new PageImpl<>(accountsWithAvatars, pageable, accountsPage.getTotalElements());
    }

    public List<Long> getSubscribersUsers(Account account) {
        if (account.getSubscribersJson() == null || account.getSubscribersJson().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(account.getSubscribersJson(), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.PROCESS_ADD_STUDIED_COURSE_FAIL);
        }
    }

    private void saveSubscribersUsers(Account account, List<Long> subscribedsUsers) {
        try {
            account.setSubscribersJson(objectMapper.writeValueAsString(subscribedsUsers));
            this.saveAccount(account);
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.PROCESS_ADD_STUDIED_COURSE_FAIL);
        }
    }

    private List<Long> getSubscribingUsers(Account account) {
        if (account.getSubscribingJson() == null || account.getSubscribingJson().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(account.getSubscribingJson(), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.PROCESS_ADD_STUDIED_COURSE_FAIL);
        }
    }

    private void saveSubscribingUsers(Account account, List<Long> subscribingUsers) {
        try {
            account.setSubscribingJson(objectMapper.writeValueAsString(subscribingUsers));
            this.saveAccount(account);
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.PROCESS_ADD_STUDIED_COURSE_FAIL);
        }
    }
}
