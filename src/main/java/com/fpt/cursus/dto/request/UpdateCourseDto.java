package com.fpt.cursus.dto.request;

import com.fpt.cursus.enums.Category;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateCourseDto {
    @Size(max = 200, message = "COURSE_SIZE_INVALID")
    private String name;
    private String description;
    private MultipartFile pictureLink;
    private Double price;
    private Category category;
}
