package com.fpt.cursus.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fpt.cursus.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Entity
@Getter
@Setter
public class OrdersDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd' 'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date createdDate;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private Double price;
    private Long courseId;
    private String createdBy;

    @ManyToOne
    @JoinColumn(name = "orders_id")
    @JsonIgnore
    private Orders orders;
}
