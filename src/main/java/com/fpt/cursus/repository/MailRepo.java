package com.fpt.cursus.repository;

import com.fpt.cursus.entity.Mail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface MailRepo extends JpaRepository<Mail, Long> {

    Optional<Mail> findByEmail(String email);
}
