package com.fpt.cursus.dto;

import lombok.Data;

import java.util.List;

@Data
public class StudiedCourse {
    private Long id;
    private List<Long> lessonIds;
}
