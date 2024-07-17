package com.fpt.cursus.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fpt.cursus.dto.object.StudiedCourse;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomAccountResDto {
    private long id;
    private List<Long> wishListCourses;
    private List<StudiedCourse> studiedCourses;
    private List<Long> enrolledCourses;
}
