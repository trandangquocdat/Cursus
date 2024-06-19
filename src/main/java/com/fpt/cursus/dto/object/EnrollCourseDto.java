package com.fpt.cursus.dto.object;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
@AllArgsConstructor

public class EnrollCourseDto {
    private String courseName;
    private String category;
    private double price;
    private float rating;

}
