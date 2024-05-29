package com.fpt.cursus.repository;

import com.fpt.cursus.entity.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface EmailRepo extends JpaRepository<Email, Long> {

    Optional<Email> findByEmail(String email);
    Email findMailByEmail(String email);

    Email findByEmailAndType(String email, String type);

}
