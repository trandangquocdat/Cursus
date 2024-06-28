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

    Optional<Otp> findByEmailAndValidTrue(String email);
    Otp findOtpByEmailAndValid(String email, Boolean valid);
    Otp findMailByEmail(String email);

    @Transactional
    @Modifying
    @Query("DELETE FROM Otp o WHERE o.valid = false OR o.otpGeneratedTime < :time")
    void deleteInvalidOrExpiredOtps(@Param("time") LocalDateTime time);


    @Transactional
    @Modifying
    @Query("UPDATE Otp SET valid = false WHERE email = :email AND valid = true")
    void updateOldOtps(@Param("email") String email);
}
