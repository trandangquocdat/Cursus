package com.fpt.cursus.repository;

import com.fpt.cursus.entity.Account;
import com.fpt.cursus.enums.InstructorStatus;
import com.fpt.cursus.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<Account> findAccountByRole(Role role, Pageable pageable);

    Page<Account> findAccountByInstructorStatus(InstructorStatus status, Pageable pageable);

    Page<Account> findByIdIn(List<Long> ids, Pageable pageable);

    Page<Account> findAccountByRoleIn(List<Role> roles, Pageable pageable);

    Page<Account> findByFullNameLikeAndInstructorStatus(String name, InstructorStatus status, Pageable pageable);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);


}
