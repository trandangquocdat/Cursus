package com.fpt.cursus.dto.request;

import com.fpt.cursus.enums.type.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCourseDto {
    @NotBlank(message = "COURSE_NAME_NULL")
    @Size(max = 200, message = "COURSE_SIZE_INVALID")
    private String name;
    private String description;
    private String pictureLink;
    @NotBlank(message = "COURSE_PRICE_NULL")
    private double price;
    @NotBlank(message = "COURSE_CATEGORY_NULL")
    private Category category;
}
