package com.fpt.cursus.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fpt.cursus.enums.FeedbackType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String content;
    private String createdBy;
    private Date createdDate;
    @Enumerated(EnumType.STRING)
    private FeedbackType type;
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "course_id")
    private Course course;
}
