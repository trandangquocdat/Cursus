package com.fpt.cursus.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fpt.cursus.enums.Category;
import com.fpt.cursus.enums.CourseStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(unique = true)
    private String name;
    private String description;
    private String pictureLink;
    private double price;
    private float rating;
    @Enumerated(EnumType.STRING)
    private Category category;
    @Enumerated(EnumType.STRING)
    private CourseStatus status;
    private Date createdDate;
    private Date updatedDate;
    private String createdBy;
    private String updatedBy;
    private float version;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Chapter> chapter;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Feedback> feedback;
}
