package com.fpt.cursus.dto.request;

import com.fpt.cursus.enums.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateCourseDto {
    @NotBlank(message = "COURSE_NAME_NULL")
    @Size(max = 200, message = "COURSE_SIZE_INVALID")
    private String name;
    private String description;
    private MultipartFile pictureLink;
    @NotNull(message = "COURSE_PRICE_NULL")
    private Double price;
    @NotNull(message = "COURSE_CATEGORY_NULL")
    private Category category;
}
