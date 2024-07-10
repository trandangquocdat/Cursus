package com.fpt.cursus.service;

import com.fpt.cursus.dto.object.StudiedCourse;
import com.fpt.cursus.dto.request.CreateCourseDto;
import com.fpt.cursus.dto.request.UpdateCourseDto;
import com.fpt.cursus.dto.response.GeneralCourse;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.enums.Category;
import com.fpt.cursus.enums.CourseStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CourseService {
    Course createCourse(CreateCourseDto createCourseDto);

    Course deleteCourseById(Long id);

    Course updateCourse(Long id, UpdateCourseDto request);

    Page<Course> getCourseByCreatedBy(int offset, int pageSize, String sortBy);

    Course approveCourseById(Long id, CourseStatus status);

    Course getCourseById(Long id);

    Page<Course> getCourseByStatus(CourseStatus status, int offset, int pageSize, String sortBy);

    Account addStudiedLesson(Long lessonId);

    Account addToWishList(List<Long> ids);

    Account removeFromWishList(Long id);

    Page<GeneralCourse> getWishListCourses(int offset, int pageSize, String sortBy);

    Page<GeneralCourse> getCourseByCategory(Category category, int offset, int pageSize, String sortBy);

    double percentDoneCourse(Long courseId);

    Page<GeneralCourse> getAllGeneralCourses(String sortBy, int offset, int pageSize);

    Page<GeneralCourse> getGeneralEnrolledCourses(String sortBy, int offset, int pageSize);

    Page<Course> getDetailEnrolledCourses(String sortBy, int offset, int pageSize);

    void saveCourse(Course course);

    Page<GeneralCourse> getGeneralCourseByName(String name, int offset, int pageSize, String sortBy);

    List<StudiedCourse> getAllStudiedCourses();

    StudiedCourse getCheckPoint();

    Page<Course> getAllCourse(int offset, int pageSize, String sortBy);
}
