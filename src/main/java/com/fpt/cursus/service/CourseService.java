package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.CreateCourseDto;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.enums.status.CourseStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CourseService {

    Course createCourse(CreateCourseDto createCourseDto);

    void deleteCourseById(Long id);

    void updateCourse(Long id, CreateCourseDto createCourseDto);

    void verifyCourseById(Long id);

    Course findCourseById(Long id);

    List<Course> findCourseByStatus(CourseStatus status);

    void addStudiedLesson(Long id, Long lessonId);

    double percentDoneCourse(Long id);

    Page<Course> getAllCourse(String sortBy, int offset, int pageSize);

    List<Course> getEnrolledCourses();

    void ratingCourse(long courseId, float rating);
}
