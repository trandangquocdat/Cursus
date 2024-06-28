package com.fpt.cursus.entity;

import com.fpt.cursus.enums.status.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private double price;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String createdBy;
    private Date createdDate;
    @Column(columnDefinition = "TEXT")
    private String orderCourseJson;
    @Transient
    private List<Long> orderCourse;


}
