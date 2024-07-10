package com.fpt.cursus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateLessonDto {
    @NotBlank(message = "LESSON_NAME_NULL")
    @Size(max = 200, message = "LESSON_SIZE_INVALID")
    private String name;
    private String description;
    private MultipartFile videoLink;
}
