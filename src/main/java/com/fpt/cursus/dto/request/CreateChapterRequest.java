package com.fpt.cursus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateChapterRequest {
    @NotBlank(message = "CHAPTER_NAME_NULL")
    @Size(max = 200, message = "CHAPTER_SIZE_INVALID")
    private String name;
    private String description;
    @NotBlank(message = "CHAPTER_COURSE_ID_NULL")
    private Long courseId;
}
