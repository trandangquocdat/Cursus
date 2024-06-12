package com.fpt.cursus.repository;

import com.fpt.cursus.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepo extends JpaRepository<Course, Long> {
    List<Course> findAll();
    boolean existsByName(String name);
    Course findCourseById(Long id);

}
