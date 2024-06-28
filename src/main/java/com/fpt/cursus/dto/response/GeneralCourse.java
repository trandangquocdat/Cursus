package com.fpt.cursus.dto.response;

import com.fpt.cursus.enums.status.CourseStatus;
import com.fpt.cursus.enums.type.Category;

import lombok.Data;

import java.util.Date;

@Data
public class GeneralCourse {
    private long id;
    private String name;
    private String description;
    private String pictureLink;
    private double price;
    private float rating;
    private Category category;
    private CourseStatus status;
    private Date createdDate;
    private Date updatedDate;
    private String createdBy;
    private String updatedBy;
    private float version = 1.0f;
}
