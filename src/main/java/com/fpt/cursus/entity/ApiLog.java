package com.fpt.cursus.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(length = 300)
    private String requestUrl;
    @Column(length = 800)
    private String queryString;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd' 'HH:mm:ss")
    private Date accessTime;
    private double count;
}
