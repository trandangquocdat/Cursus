package com.fpt.cursus.repository;


import com.fpt.cursus.entity.Course;
import com.fpt.cursus.enums.status.CourseStatus;
import org.hibernate.annotations.Where;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CourseRepo extends JpaRepository<Course, Long> {

    Page<Course> findAll(Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.status = :status")
    Page<Course> findAllByStatus(@Param("status") CourseStatus status, Pageable pageable);

    List<Course> findByIdIn(List<Long> id);

    boolean existsByName(String name);

    List<Course> findCourseByStatus(CourseStatus status);

    Course findCourseById(Long id);
}
