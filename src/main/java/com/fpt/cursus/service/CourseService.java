package com.fpt.cursus.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.dto.StudiedCourse;
import com.fpt.cursus.dto.request.CreateCourseDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.enums.status.CourseStatus;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.AccountRepo;
import com.fpt.cursus.repository.CourseRepo;
import com.fpt.cursus.util.AccountUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CourseService {
    @Autowired
    private CourseRepo courseRepo;
    @Autowired
    private AccountUtil accountUtil;
    @Autowired
    private AccountRepo accountRepo;

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

    public void verifyCourseById(Long id) {
        Course course = courseRepo.findCourseById(id);
        if (course == null) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
        course.setStatus(CourseStatus.PUBLISHED);
        courseRepo.save(course);
    }

    public Course findCourseById(Long id) {
        Course course = courseRepo.findCourseById(id);
        if (course == null) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
        return course;
    }

    public List<Course> findCourseByStatus(CourseStatus status) {
        List<Course> courses = courseRepo.findCourseByStatus(status);
        if (courses == null) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
        return courses;
    }

    public void addStudiedLesson(Long id, Long lessonId) {
        ObjectMapper mapper = new ObjectMapper();
        Account account = accountUtil.getCurrentAccount();
        try {
            if (account.getStudiedCourse() == null) {
                List<StudiedCourse> studiedCourse = new ArrayList<>();
                StudiedCourse course = new StudiedCourse();
                course.setId(id);
                course.setLessonIds(List.of(lessonId));
                studiedCourse.add(course);
                account.setStudiedCourse(studiedCourse);
                account.setStudiedCourseJson(mapper.writeValueAsString(account.getStudiedCourse()));
                accountRepo.save(account);
                return;
            }
            account.setStudiedCourse(mapper.readValue(account.getStudiedCourseJson(), new TypeReference<>() {
            }));
            for (StudiedCourse course : account.getStudiedCourse()) {
                if (course.getId() == id) {
                    List<Long> lessonIds = course.getLessonIds();
                    lessonIds.add(lessonId);
                    course.setLessonIds(lessonIds);
                    break;
                }
            }
            account.setStudiedCourseJson(mapper.writeValueAsString(account.getStudiedCourse()));
            accountRepo.save(account);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public double percentDoneCourse(Long id) {
        Course course = courseRepo.findCourseById(id);
        if (course == null) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
        Account account = accountUtil.getCurrentAccount();
        ObjectMapper mapper = new ObjectMapper();
        try {
            account.setStudiedCourse(mapper.readValue(account.getStudiedCourseJson(), new TypeReference<>() {
            }));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        for (StudiedCourse studiedCourse : account.getStudiedCourse()) {
            if (studiedCourse.getId() == id) {
                float totalLesson = getTotalLesson(id);
                return (double) studiedCourse.getLessonIds().size() / totalLesson;
            }
        }
        return 0;

    }
    private int getTotalLesson(Long id) {
        Course course = courseRepo.findCourseById(id);
        if (course == null) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
        int totalLesson = 0;
        for (int i = 0; i < course.getChapter().size(); i++) {
            totalLesson += course.getChapter().get(i).getLesson().size();
        }
        return totalLesson;
    }
    public Page<Course> findAllCourseWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return courseRepo.findAll(pageable);
    }

    public Page<Course> findAllCourseWithPaginationAndSort(String sortBy, int offset, int pageSize) {
        return courseRepo.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(sortBy)));
    }

}


