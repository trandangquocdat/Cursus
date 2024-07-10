package com.fpt.cursus.dto.object;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

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
