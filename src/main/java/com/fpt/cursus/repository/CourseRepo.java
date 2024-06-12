package com.fpt.cursus.repository;

import com.fpt.cursus.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepo extends JpaRepository<Course, Long> {
    List<Course> findByNameIn(List<String> names);
}
