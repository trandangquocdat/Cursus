package com.fpt.cursus.dto.request;

import com.fpt.cursus.enums.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCouseDto {
    @NotEmpty(message = "Please input course name")
    @Size(max = 200, message = "Course name must be less than 200 characters")
    private String name;
    private String description;
    private String pictureLink;
    @NotEmpty(message = "Please input price of course")
    private double price;
    @NotEmpty(message = "Please select category of course")
    private Category category;
}
