package com.fpt.cursus.repository;

import com.fpt.cursus.entity.Course;
import com.fpt.cursus.enums.type.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.fpt.cursus.enums.status.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;


@Repository
public interface CourseRepo extends JpaRepository<Course, Long> {


    Page<Course> findAllByStatus(CourseStatus status, Pageable pageable);

    Page<Course> findByIdIn(List<Long> id, Pageable pageable);
    List<Course> findByIdIn(List<Long> id);

    boolean existsByName(String name);
    Page<Course> findCourseByCreatedBy(String createdBy, Pageable pageable);
    Page<Course> findCourseByStatus(CourseStatus status, Pageable pageable);

    Page<Course> findCourseByCategoryAndStatus(Category category,CourseStatus status, Pageable pageable);
}
