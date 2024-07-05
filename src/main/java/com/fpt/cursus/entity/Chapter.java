package com.fpt.cursus.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fpt.cursus.enums.ChapterStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    private ChapterStatus status;
    private Date createdDate;
    private Date updatedDate;
    private String createdBy;
    private String updatedBy;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "course_id")
    private Course course;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL)
    private List<Lesson> lesson;
}
