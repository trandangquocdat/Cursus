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

    Page<Course> findAll(Pageable pageable);

    @Query(value = "SELECT * FROM Course c WHERE c.status = :status",
            countQuery = "SELECT count(*) FROM Course c WHERE c.status = :status",
            nativeQuery = true)
    Page<Course> findAllByStatus(@Param("status") String status, Pageable pageable);

    Page<Course> findAllByStatus(CourseStatus status, Pageable pageable);

    Page<Course> findByIdIn(List<Long> id, Pageable pageable);

    boolean existsByName(String name);

    Page<Course> findCourseByStatus(CourseStatus status, Pageable pageable);

    Course findCourseById(Long id);

    Page<Course> findCourseByCategory(Category category, Pageable pageable);
}
