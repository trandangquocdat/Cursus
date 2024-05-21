package com.fpt.cursus.service;

import com.fpt.cursus.dto.LoginReqDto;
import com.fpt.cursus.dto.LoginResDto;
import com.fpt.cursus.dto.RegisterReqDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.enums.AccountStatus;
import com.fpt.cursus.repository.AccountRepo;
import com.fpt.cursus.util.AccountUtil;
import com.fpt.cursus.util.TokenHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    @Autowired
    AccountRepo accountRepo;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    TokenHandler tokenHandler;
    @Autowired
    AccountUtil accountUtil;

    public Account register(RegisterReqDto registerReqDTO) {
        Account account = new Account();
        String rawPassword = registerReqDTO.getPassword();
        account.setUsername(registerReqDTO.getUsername());
        account.setPassword(passwordEncoder.encode(rawPassword));
        account.setEmail(registerReqDTO.getEmail());
        account.setFullName(registerReqDTO.getFullName());
        account.setAvatar(registerReqDTO.getAvatar());
        account.setRole(registerReqDTO.getRole());
        account.setPhone(registerReqDTO.getPhone());
        account.setStatus(AccountStatus.ACTIVE);
        Account newAccount = accountRepo.save(account);
        return newAccount;
    }
    public LoginResDto login(LoginReqDto loginReqDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginReqDto.getUsername(), loginReqDto.getPassword())
            );
            Account account = (Account) authentication.getPrincipal();
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
}
