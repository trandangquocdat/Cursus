package com.fpt.cursus.dto.request;

import lombok.Data;

@Data
public class CreateChapterRequest {
    private String name;
    private String description;
    private Long courseId;
}
