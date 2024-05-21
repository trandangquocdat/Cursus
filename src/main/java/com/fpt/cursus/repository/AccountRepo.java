package com.fpt.cursus.repository;

import com.fpt.cursus.entity.Account;
import com.fpt.cursus.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AccountRepo extends JpaRepository<Account, Long> {
    Optional<Account> findByUsername(String username);

    Account findAccountByEmail(String email);

    Account findAccountById(Long id);
    Account findAccountByUsername(String username);

    Account findByEmail(String email);

    Account findAccountByRole(Role role);
}
