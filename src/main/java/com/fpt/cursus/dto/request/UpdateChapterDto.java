package com.fpt.cursus.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateChapterDto {
    @Size(max = 200, message = "CHAPTER_SIZE_INVALID")
    private String name;
    private String description;
}
