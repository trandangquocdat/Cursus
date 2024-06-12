package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.CreateCouseDto;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.enums.Category;
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
    
    public Course createCourse(CreateCouseDto createCouseDto) {
        Date now = new Date();
        Course course = new Course();
        course.setName(createCouseDto.getName());
        course.setDescription(createCouseDto.getDescription());
        course.setPictureLink(createCouseDto.getPictureLink());
        course.setPrice(createCouseDto.getPrice());
        course.setCategory(createCouseDto.getCategory());
        course.setCreatedDate(now);
        course.setCreatedBy(accountUtil.getCurrentAccount().getUsername());
        course.setStatus(CourseStatus.DRAFT);
        return courseRepo.save(course);
    }

    public void deleteCourseById(Long id) {
        Course course = courseRepo.findCourseById(id);
        course.setStatus(CourseStatus.DELETED);
        courseRepo.save(course);
    }

    public Course updateCourse(Long id, CreateCouseDto createCouseDto) {
        Course existingCourse = courseRepo.findCourseById(id);
        if (existingCourse != null) {
            Date date = new Date();
            existingCourse.setName(createCouseDto.getName());
            existingCourse.setPrice(createCouseDto.getPrice());
            existingCourse.setPictureLink(createCouseDto.getPictureLink());
            existingCourse.setDescription(createCouseDto.getDescription());
            existingCourse.setCategory(createCouseDto.getCategory());
            existingCourse.setUpdatedBy(accountUtil.getCurrentAccount().getUsername());
            existingCourse.setStatus(CourseStatus.DRAFT);
            existingCourse.setUpdatedDate(date);
            return courseRepo.save(existingCourse);
        }
        return null;
    }

    public List<Course> findAllCourseWithPagination(int offset, int pageSize) {
        return courseRepo.findAll(PageRequest.of(offset, pageSize)).getContent();
    }
    public List<Course> findAllCourseWithPaginationAndSort(String sortBy, int offset, int pageSize) {
        return courseRepo.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(sortBy))).getContent();
    }

    public List<Course> findCourseByCategory(String category) {
        return courseRepo.findCourseByCategory(Category.getCategory(category));
    }
}
