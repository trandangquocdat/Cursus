package com.fpt.cursus.config;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.enums.UserStatus;
import com.fpt.cursus.enums.Role;
import com.fpt.cursus.service.AccountService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.logging.Logger;

@Configuration
public class InitialSetupConfig {

    private final AccountService accountService;

    private final PasswordEncoder passwordEncoder;

    public InitialSetupConfig(AccountService accountService, PasswordEncoder passwordEncoder) {
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public ApplicationRunner initializer() {
        Logger logger = Logger.getLogger(getClass().getName());
        return args -> {
            // Check if admin account exists
            if (!accountService.existAdmin("admin")) {
                // Create a new admin account
                Account adminAccount = new Account();
                adminAccount.setUsername("admin");
                adminAccount.setEmail("admin@gmail.com");
                adminAccount.setCreatedDate(new Date());
                adminAccount.setFullName("Administrator");
                adminAccount.setPhone("0972340212");
                adminAccount.setPassword(passwordEncoder.encode("admin"));
                adminAccount.setRole(Role.ADMIN);
                adminAccount.setStatus(UserStatus.ACTIVE);
                adminAccount.setAvatar(""); // Set default or empty avatar
                // Save the admin account
                accountService.saveAccount(adminAccount);
                logger.info("Admin account created: admin/admin");
            } else {
                logger.info("Admin account already exists: admin/admin");
            }
        };
    }
}
