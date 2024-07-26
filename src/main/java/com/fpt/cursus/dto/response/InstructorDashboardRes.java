package com.fpt.cursus.dto.response;

import lombok.Data;

@Data
public class InstructorDashboardRes {
    private long totalCourses;
    private long totalEnroll;
    private double totalSales;
    private long totalStudents;
    private long currentSubscribers;
}

