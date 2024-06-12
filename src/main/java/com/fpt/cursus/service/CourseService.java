package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.CreateCourseDto;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.enums.status.CourseStatus;
import com.fpt.cursus.repository.CourseRepo;
import com.fpt.cursus.util.AccountUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CourseService {
    @Autowired
    private CourseRepo courseRepo;
    @Autowired
    private AccountUtil accountUtil;


    public Course createCourse(CreateCourseDto createCourseDto) {
        if (courseRepo.existsByName(createCourseDto.getName())) {
            throw new AppException(ErrorCode.COURSE_EXISTS);
        }
        Date now = new Date();
        Course course = new Course();
        course.setName(createCourseDto.getName());
        course.setDescription(createCourseDto.getDescription());
        course.setPictureLink(createCourseDto.getPictureLink());
        course.setPrice(createCourseDto.getPrice());
        course.setCategory(createCourseDto.getCategory());
        course.setCreatedDate(now);
        course.setCreatedBy(accountUtil.getCurrentAccount().getUsername());
        course.setStatus(CourseStatus.DRAFT);
        return courseRepo.save(course);
    }

    public void deleteCourseById(Long id) {
        Course course = courseRepo.findCourseById(id);
        if (course != null) {
            Date date = new Date();
            course.setUpdatedBy(accountUtil.getCurrentAccount().getUsername());
            course.setUpdatedDate(date);
            course.setStatus(CourseStatus.DELETED);
            courseRepo.save(course);
        } else {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }

    }

    public Course updateCourse(Long id, CreateCourseDto createCourseDto) {
        Course existingCourse = courseRepo.findCourseById(id);

        if (existingCourse != null && existingCourse.getStatus() != CourseStatus.DELETED) {
            if (courseRepo.existsByName(createCourseDto.getName())) {
                throw new AppException(ErrorCode.COURSE_EXISTS);
            }
            Date date = new Date();
            existingCourse.setName(createCourseDto.getName());
            existingCourse.setPrice(createCourseDto.getPrice());
            existingCourse.setPictureLink(createCourseDto.getPictureLink());
            existingCourse.setDescription(createCourseDto.getDescription());
            existingCourse.setCategory(createCourseDto.getCategory());
            existingCourse.setUpdatedBy(accountUtil.getCurrentAccount().getUsername());
            existingCourse.setStatus(CourseStatus.DRAFT);
            existingCourse.setUpdatedDate(date);
            return courseRepo.save(existingCourse);
        } else {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
    }

    public Course findCourseById(Long id) {
        Course course = courseRepo.findCourseById(id);
        if (course == null) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
        return course;
    }

    public List<Course> findAllCourseWithPagination(int offset, int pageSize) {
        return courseRepo.findAll(PageRequest.of(offset, pageSize)).getContent();
    }

    public List<Course> findAllCourseWithPaginationAndSort(String sortBy, int offset, int pageSize) {
        return courseRepo.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(sortBy))).getContent();
    }
}


