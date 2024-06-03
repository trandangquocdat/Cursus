package com.fpt.cursus.repository;

import com.fpt.cursus.entity.Otp;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
@Repository
public interface OtpRepo extends JpaRepository<Otp, Long> {

    Optional<Otp> findByEmail(String email);
    Otp findMailByEmail(String email);

    Otp findByEmailAndType(String email, String type);

    @Transactional
    @Modifying
    @Query("DELETE FROM Otp WHERE email = :email OR otpGeneratedTime > :time")
    void deleteOldOtps(@Param("email") String email, @Param("time") LocalDateTime time);


}
