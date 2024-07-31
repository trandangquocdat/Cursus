package com.fpt.cursus.service;

import com.fpt.cursus.dto.object.StudiedCourse;
import com.fpt.cursus.dto.request.CreateCourseDto;
import com.fpt.cursus.dto.request.UpdateCourseDto;
import com.fpt.cursus.dto.response.CustomAccountResDto;
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

    List<Course> getCourseByCreatedBy(String username);

    Course approveCourseById(Long id, CourseStatus status);

    Course getCourseById(Long id);

    Page<Course> getCourseByStatus(CourseStatus status, int offset, int pageSize, String sortBy);

    CustomAccountResDto addStudiedLesson(Long lessonId);

    CustomAccountResDto addToWishList(List<Long> ids);

    CustomAccountResDto removeFromWishList(Long id);

    Page<GeneralCourse> getWishListCourses(int offset, int pageSize, String sortBy);

    Page<GeneralCourse> getCourseByCategory(Category category, int offset, int pageSize, String sortBy);

    double percentDoneCourse(Long courseId);

    Page<GeneralCourse> getAllGeneralCourses(String sortBy, int offset, int pageSize);

    Page<GeneralCourse> getGeneralEnrolledCourses(String sortBy, int offset, int pageSize);

    Page<Course> getDetailEnrolledCourses(String sortBy, int offset, int pageSize);

    void saveCourse(Course course);

    Page<GeneralCourse> getGeneralCourseByName(String name, int offset, int pageSize, String sortBy);

    List<StudiedCourse> getAllStudiedCourses();

    List<StudiedCourse> getStudiedCourses(Account account);

    StudiedCourse getCheckPoint();

    Page<Course> getAllCourse(int offset, int pageSize, String sortBy);

    List<Course> getCourseByIdsIn(List<Long> courseIds);

    List<Category> getAllCategory();

    GeneralCourse getGeneralCourseById(Long id);

    Course getDetailCourseById(Long id);
}
