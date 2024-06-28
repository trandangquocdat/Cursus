package com.fpt.cursus.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fpt.cursus.enums.status.LessonStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    private LessonStatus status;
    private String videoLink;
    private Date createdDate;
    private Date updatedDate;
    private String createdBy;
    private String updatedBy;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;
}
