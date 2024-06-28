package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.CreateCourseDto;
import com.fpt.cursus.dto.response.GeneralCourse;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.enums.status.CourseStatus;
import com.fpt.cursus.enums.type.Category;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CourseService {

    Course createCourse(CreateCourseDto createCourseDto);

    void deleteCourseById(Long id);

    void updateCourse(Long id, CreateCourseDto createCourseDto);

    Page<Course> getCourseByCreatedBy(int offset, int pageSize, String sortBy);

    void verifyCourseById(Long id, CourseStatus status);

    Course getCourseById(Long id);

    Page<Course> getCourseByStatus(CourseStatus status, int offset, int pageSize, String sortBy);

    void checkOffset(int offset);

    void addStudiedLesson(Long courseId, Long lessonId);

    void addToWishList(List<Long> ids);

    void removeFromWishList(Long id);

    Page<GeneralCourse> getWishListCourses(int offset, int pageSize, String sortBy);

    Page<GeneralCourse> getCourseByCategory(Category category,
                                            int offset,
                                            int pageSize,
                                            String sortBy);

    double percentDoneCourse(Long courseId);

    Page<GeneralCourse> getAllGeneralCourses(String sortBy, int offset, int pageSize);

    Page<GeneralCourse> getGeneralEnrolledCourses(String sortBy, int offset, int pageSize);

    Page<Course> getDetailEnrolledCourses(String sortBy, int offset, int pageSize);

    void saveCourse(Course course);
}
