package com.fpt.cursus.repository;

import com.fpt.cursus.entity.Account;
import com.fpt.cursus.enums.type.InstructorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepo extends JpaRepository<Account, Long> {
    Optional<Account> findByUsername(String username);

    Optional<Account> findByEmail(String email);

    Account findAccountByEmail(String email);

    Account findAccountByUsername(String username);

    List<Account> findAccountByInstructorStatus(InstructorStatus status);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);


}
