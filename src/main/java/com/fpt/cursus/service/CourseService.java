package com.fpt.cursus.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.dto.object.StudiedCourse;
import com.fpt.cursus.dto.request.CreateCourseDto;
import com.fpt.cursus.dto.response.GeneralCourse;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.enums.status.CourseStatus;
import com.fpt.cursus.enums.type.Category;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.CourseRepo;
import com.fpt.cursus.util.AccountUtil;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CourseService {

    private final CourseRepo courseRepo;

    private final AccountUtil accountUtil;

    private final AccountService accountService;

    private final ObjectMapper objectMapper;

    public CourseService(AccountUtil accountUtil, AccountService accountService, CourseRepo courseRepo, ObjectMapper objectMapper) {
        this.accountUtil = accountUtil;
        this.accountService = accountService;
        this.courseRepo = courseRepo;
        this.objectMapper = objectMapper;
    }

    public Course createCourse(CreateCourseDto createCourseDto) {
        validateCourseDto(createCourseDto);

        Course course = new Course();
        course.setName(createCourseDto.getName());
        course.setDescription(createCourseDto.getDescription());
        course.setPictureLink(createCourseDto.getPictureLink());
        course.setPrice(createCourseDto.getPrice());
        course.setCategory(createCourseDto.getCategory());
        course.setCreatedDate(new Date());
        course.setCreatedBy(accountUtil.getCurrentAccount().getUsername());
        course.setStatus(CourseStatus.DRAFT);

        return courseRepo.save(course);
    }

    public void deleteCourseById(Long id) {
        Course course = findCourseById(id);
        course.setUpdatedBy(accountUtil.getCurrentAccount().getUsername());
        course.setUpdatedDate(new Date());
        course.setStatus(CourseStatus.DELETED);
        courseRepo.save(course);
    }

    public void updateCourse(Long id, CreateCourseDto createCourseDto) {
        Course existingCourse = findCourseById(id);
        if (existingCourse.getStatus() == CourseStatus.DELETED) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }

        validateCourseDto(createCourseDto);

        existingCourse.setName(createCourseDto.getName());
        existingCourse.setPrice(createCourseDto.getPrice());
        existingCourse.setPictureLink(createCourseDto.getPictureLink());
        existingCourse.setDescription(createCourseDto.getDescription());
        existingCourse.setCategory(createCourseDto.getCategory());
        existingCourse.setUpdatedBy(accountUtil.getCurrentAccount().getUsername());
        existingCourse.setStatus(CourseStatus.DRAFT);
        existingCourse.setVersion(existingCourse.getVersion() + 0.1f);
        existingCourse.setUpdatedDate(new Date());

        courseRepo.save(existingCourse);
    }

    public void verifyCourseById(Long id) {
        Course course = findCourseById(id);
        course.setStatus(CourseStatus.PUBLISHED);
        courseRepo.save(course);
    }

    public Course findCourseById(Long id) {
        return courseRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
    }

    public Page<Course> findCourseByStatus(CourseStatus status, int offset, int pageSize, String sortBy) {
        if (sortBy != null) {
            Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(sortBy));
            Page<Course> courses = courseRepo.findCourseByStatus(status, pageable);
            if (courses.isEmpty()) {
                throw new AppException(ErrorCode.COURSE_NOT_FOUND);
            }
            return courses;
        }
        Pageable pageable = PageRequest.of(offset, pageSize);
        Page<Course> courses = courseRepo.findCourseByStatus(status, pageable);
        if (courses.isEmpty()) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
        return courses;
    }

    public void addStudiedLesson(Long courseId, Long lessonId) {
        Account account = accountUtil.getCurrentAccount();
        List<StudiedCourse> studiedCourses = getStudiedCourses(account);

        studiedCourses.stream()
                .filter(sc -> sc.getId().equals(courseId))
                .findFirst()
                .ifPresentOrElse(
                        sc -> sc.getLessonIds().add(lessonId),
                        () -> {
                            StudiedCourse newCourse = new StudiedCourse();
                            newCourse.setId(courseId);
                            newCourse.setLessonIds(List.of(lessonId));
                            studiedCourses.add(newCourse);
                        }
                );

        saveStudiedCourses(account, studiedCourses);
    }

    public Page<Course> findCourseByCategory(Category category, int offset, int pageSize, String sortBy) {
        if (sortBy != null) {
            Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(sortBy));
            Page<Course> courses = courseRepo.findCourseByCategory(category, pageable);
            if (courses.isEmpty()) {
                throw new AppException(ErrorCode.COURSE_NOT_FOUND);
            }
            return courses;
        }
        Pageable pageable = PageRequest.of(offset, pageSize);
        Page<Course> courses;
        if (category == Category.ALL) {
            courses = courseRepo.findAllByStatus(CourseStatus.PUBLISHED, pageable);
        } else {
            courses = courseRepo.findCourseByCategory(category, pageable);
        }
        if (courses.isEmpty()) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
        return courses;
    }

    public double percentDoneCourse(Long courseId) {
        Account account = accountUtil.getCurrentAccount();
        List<StudiedCourse> studiedCourses = getStudiedCourses(account);

        return studiedCourses.stream()
                .filter(sc -> sc.getId().equals(courseId))
                .findFirst()
                .map(sc -> {
                    int totalLessons = getTotalLesson(courseId);
                    return totalLessons == 0 ? 0 : (double) sc.getLessonIds().size() / totalLessons;
                })
                .orElse(0.0);
    }

    private int getTotalLesson(Long courseId) {
        Course course = findCourseById(courseId);
        return course.getChapter().stream()
                .mapToInt(chapter -> chapter.getLesson().size())
                .sum();
    }

    public Page<GeneralCourse> getAllGeneralCourses(String sortBy, int offset, int pageSize) {
        Pageable pageable = getPageable(sortBy, offset, pageSize);
        Page<Course> courses = courseRepo.findAllByStatus(CourseStatus.PUBLISHED.name(), pageable);
        return convertToGeneralCoursePage(courses);
    }

    public Page<GeneralCourse> getGeneralEnrolledCourses(String sortBy, int offset, int pageSize) {
        Page<Course> courses = getEnrolledCoursesPage(offset, pageSize);
        Pageable pageable = getPageable(sortBy, offset, pageSize);
        return convertToGeneralCoursePage(new PageImpl<>(courses.getContent(), pageable, courses.getTotalElements()));
    }

    public Page<Course> getDetailEnrolledCourses(String sortBy, int offset, int pageSize) {
        Page<Course> courses = getEnrolledCoursesPage(offset, pageSize);
        Pageable pageable = getPageable(sortBy, offset, pageSize);
        return new PageImpl<>(courses.getContent(), pageable, courses.getTotalElements());
    }

    public void saveCourse(Course course) {
        courseRepo.save(course);
    }

    private Pageable getPageable(String sortBy, int offset, int pageSize) {
        return sortBy == null
                ? PageRequest.of(offset, pageSize)
                : PageRequest.of(offset, pageSize, Sort.by(sortBy));
    }

    private Page<GeneralCourse> convertToGeneralCoursePage(Page<Course> courses) {
        List<GeneralCourse> generalCoursesList = new ArrayList<>();
        for (Course course : courses) {
            generalCoursesList.add(convertToGeneralCourse(course));
        }
        return new PageImpl<>(generalCoursesList, courses.getPageable(), courses.getTotalElements());
    }

    private GeneralCourse convertToGeneralCourse(Course course) {
        GeneralCourse generalCourse = new GeneralCourse();
        generalCourse.setId(course.getId());
        generalCourse.setName(course.getName());
        generalCourse.setPictureLink(course.getPictureLink());
        generalCourse.setPrice(course.getPrice());
        generalCourse.setRating(course.getRating());
        generalCourse.setCategory(course.getCategory());
        generalCourse.setStatus(course.getStatus());
        generalCourse.setCreatedDate(course.getCreatedDate());
        generalCourse.setUpdatedDate(course.getUpdatedDate());
        generalCourse.setCreatedBy(course.getCreatedBy());
        generalCourse.setUpdatedBy(course.getUpdatedBy());
        generalCourse.setVersion(course.getVersion());
        return generalCourse;
    }

    private Page<Course> getEnrolledCoursesPage(int offset, int pageSize) {
        Account account = accountUtil.getCurrentAccount();
        if (account.getEnrolledCourseJson() == null || account.getEnrolledCourseJson().isEmpty()) {
            throw new AppException(ErrorCode.USER_ENROLLED_EMPTY);
        }

        List<Long> enrolledCourseIds;
        try {
            enrolledCourseIds = objectMapper.readValue(account.getEnrolledCourseJson(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(offset, pageSize);
        return courseRepo.findByIdIn(enrolledCourseIds, pageable);
    }

    private List<StudiedCourse> getStudiedCourses(Account account) {
        if (account.getStudiedCourseJson() == null || account.getStudiedCourseJson().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(account.getStudiedCourseJson(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.PROCESS_ADD_STUDIED_COURSE_FAIL);
        }
    }

    private void saveStudiedCourses(Account account, List<StudiedCourse> studiedCourses) {
        try {
            account.setStudiedCourseJson(objectMapper.writeValueAsString(studiedCourses));
            accountService.saveAccount(account);
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.PROCESS_ADD_STUDIED_COURSE_FAIL);
        }
    }

    private void validateCourseDto(CreateCourseDto createCourseDto) {
        if (courseRepo.existsByName(createCourseDto.getName())) {
            throw new AppException(ErrorCode.COURSE_EXISTS);
        }
        if (createCourseDto.getPrice() < 5000) {
            throw new AppException(ErrorCode.COURSE_PRICE_INVALID);
        }
    }
}
