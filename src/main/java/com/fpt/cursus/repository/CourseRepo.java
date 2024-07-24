package com.fpt.cursus.repository;

import com.fpt.cursus.entity.Course;
import com.fpt.cursus.enums.Category;
import com.fpt.cursus.enums.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository

public interface CourseRepo extends JpaRepository<Course, Long> {


    Page<Course> findAllByStatus(CourseStatus status, Pageable pageable);

    Page<Course> findByIdInAndStatus(List<Long> id, CourseStatus status, Pageable pageable);

    List<Course> findByIdIn(List<Long> id);

    List<Course> findCourseByNameLike(String partialName);

    boolean existsByName(String name);

    Page<Course> findCourseByCreatedBy(String createdBy, Pageable pageable);
    List<Course> findCourseByCreatedBy(String createdBy);

    Page<Course> findCourseByStatus(CourseStatus status, Pageable pageable);

    Page<Course> findCourseByCategoryAndStatus(Category category, CourseStatus status, Pageable pageable);
}
