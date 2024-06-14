package com.fpt.cursus.repository;

import com.fpt.cursus.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CourseRepo extends JpaRepository<Course, Long> {
    Page<Course> findAll(Pageable pageable);
    boolean existsByName(String name);
    Course findCourseById(Long id);

}
