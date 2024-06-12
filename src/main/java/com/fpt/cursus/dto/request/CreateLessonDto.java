package com.fpt.cursus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateLessonDto {
    @NotBlank(message = "LESSON_NAME_NULL")
    @Size(max = 200, message = "LESSON_SIZE_INVALID")
    private String name;
    private String description;
    private String videoLink;
    @NotBlank(message = "LESSON_CHAPTER_ID_NULL")
    private Long chapterId;
}
