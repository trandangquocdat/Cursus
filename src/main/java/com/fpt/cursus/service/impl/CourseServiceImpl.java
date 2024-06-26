package com.fpt.cursus.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.dto.object.StudiedCourse;
import com.fpt.cursus.dto.request.CreateCourseDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.entity.Feedback;
import com.fpt.cursus.enums.status.CourseStatus;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.AccountRepo;
import com.fpt.cursus.repository.CourseRepo;
import com.fpt.cursus.repository.FeedbackRepo;
import com.fpt.cursus.service.CourseService;
import com.fpt.cursus.util.AccountUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {
    @Autowired
    private CourseRepo courseRepo;
    @Autowired
    private AccountUtil accountUtil;
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private FeedbackRepo feedbackRepo;

    public Course createCourse(CreateCourseDto createCourseDto) {
        if (courseRepo.existsByName(createCourseDto.getName())) {
            throw new AppException(ErrorCode.COURSE_EXISTS);
        }
        if (createCourseDto.getPrice() < 5000) {
            throw new AppException(ErrorCode.COURSE_PRICE_INVALID);
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

    public void updateCourse(Long id, CreateCourseDto createCourseDto) {
        Course existingCourse = courseRepo.findCourseById(id);
        if (existingCourse != null && existingCourse.getStatus() != CourseStatus.DELETED) {
            if (courseRepo.existsByName(createCourseDto.getName())) {
                throw new AppException(ErrorCode.COURSE_EXISTS);
            }
            if (createCourseDto.getPrice() < 5000) {
                throw new AppException(ErrorCode.COURSE_PRICE_INVALID);
            }
            Date date = new Date();
            existingCourse.setName(createCourseDto.getName());
            existingCourse.setPrice(createCourseDto.getPrice());
            existingCourse.setPictureLink(createCourseDto.getPictureLink());
            existingCourse.setDescription(createCourseDto.getDescription());
            existingCourse.setCategory(createCourseDto.getCategory());
            existingCourse.setUpdatedBy(accountUtil.getCurrentAccount().getUsername());
            existingCourse.setStatus(CourseStatus.DRAFT);
            existingCourse.setVersion((float) (existingCourse.getVersion() + 0.1));
            existingCourse.setUpdatedDate(date);
            courseRepo.save(existingCourse);
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
            throw new AppException(ErrorCode.PROCESS_ADD_STUDIED_COURSE_FAIL);
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
            throw new AppException(ErrorCode.PROCESS_CALCULATE_PERCENT_FAIL);
        }
        for (StudiedCourse studiedCourse : account.getStudiedCourse()) {
            if (studiedCourse.getId() == id) {
                float totalLesson = getTotalLesson(id);
                if (totalLesson == 0) {
                    return 0;
                }
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

    public Page<Course> getAllCourse(String sortBy, int offset, int pageSize) {
        if (sortBy == null) {
            return courseRepo.findAllByStatus("PUBLISHED", PageRequest.of(offset - 1, pageSize));
        }
        return courseRepo.findAllByStatus("PUBLISHED",
                PageRequest.of(offset, pageSize).withSort(Sort.by(sortBy)));
    }

    public List<Course> getEnrolledCourses() {
        Account account = accountUtil.getCurrentAccount();
        if (account == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        if (account.getEnrolledCourseJson() == null || account.getEnrolledCourseJson().isEmpty()) {
            throw new AppException(ErrorCode.USER_ENROLLED_EMPTY);
        }
        ObjectMapper mapper = new ObjectMapper();
        List<Long> enrolledCoursesId = null;
        try {
            enrolledCoursesId = mapper.readValue(account.getEnrolledCourseJson(), new TypeReference<>() {

            });
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
        return courseRepo.findByIdIn(enrolledCoursesId);
    }

    @Async
    public void ratingCourse(long courseId, float rating) {
        List<Feedback> feedbacks = feedbackRepo.findFeedbackByCourseId(courseId);
        float sum = 0;
        for (Feedback feedback : feedbacks) {
            sum += feedback.getRating();
        }
        sum += rating;
        Course course = courseRepo.findCourseById(courseId);
        course.setRating((float) (Math.round(sum / (feedbacks.size() + 1) * 10.0) / 10.0));
        courseRepo.save(course);
    }

}


