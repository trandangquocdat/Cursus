package com.fpt.cursus.repository;

import com.fpt.cursus.entity.Course;
import com.fpt.cursus.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepo extends JpaRepository<Course, Long> {
    List<Course> findAll();

    Course findCourseById(Long id);

    List<Course> findCourseByCategory(Category category);
}
