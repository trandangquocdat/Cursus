package com.fpt.cursus.dto.object;

import lombok.Data;


@Data
public class StudiedCourse {
    private Long courseId;
    private Long chapterId;
    private Long lessonId;
    private boolean checkPoint = false;
}
