package com.fpt.cursus.entity;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.TimeZoneColumn;

import java.time.LocalDateTime;
@Entity
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String otp;
    private Boolean valid = true;
    private LocalDateTime otpGeneratedTime;
}
