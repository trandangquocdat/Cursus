package com.fpt.cursus.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.dto.object.StudiedCourse;
import com.fpt.cursus.dto.request.CreateCourseDto;
import com.fpt.cursus.dto.request.UpdateCourseDto;
import com.fpt.cursus.dto.response.GeneralCourse;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.enums.CourseStatus;
import com.fpt.cursus.enums.Category;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.CourseRepo;
import com.fpt.cursus.util.AccountUtil;
import com.fpt.cursus.util.PageUtil;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class CourseService {

    private final CourseRepo courseRepo;
    private final AccountUtil accountUtil;
    private final AccountService accountService;
    private final ObjectMapper objectMapper;
    private final LessonService lessonService;
    private final PageUtil pageUtil;
    private final ModelMapper modelMapper;
    private final FileService fileService;

    public CourseService(AccountUtil accountUtil, AccountService accountService,
                         CourseRepo courseRepo, ObjectMapper objectMapper,
                         LessonService lessonService, PageUtil pageUtil,
                         ModelMapper modelMapper, FileService fileService) {
        this.accountUtil = accountUtil;
        this.accountService = accountService;
        this.courseRepo = courseRepo;
        this.objectMapper = objectMapper;
        this.lessonService = lessonService;
        this.pageUtil = pageUtil;
        this.modelMapper = modelMapper;
        this.fileService = fileService;
    }

    public Course createCourse(CreateCourseDto createCourseDto) {
        validateCourseDto(createCourseDto);
        Course course = modelMapper.map(createCourseDto, Course.class);
        Date date = new Date();
        course.setCreatedDate(date);
        course.setCreatedBy(accountUtil.getCurrentAccount().getUsername());
        fileService.setPicture(createCourseDto.getPictureLink(),course);
        course.setStatus(CourseStatus.DRAFT);
        course.setVersion(1f);
        return courseRepo.save(course);

    }

    public Course deleteCourseById(Long id) {
        Course course = getCourseById(id);
        course.setUpdatedBy(accountUtil.getCurrentAccount().getUsername());
        course.setUpdatedDate(new Date());
        course.setStatus(CourseStatus.DELETED);
        return courseRepo.save(course);
    }

    public Course updateCourse(Long id, UpdateCourseDto request) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true);

        Course existingCourse = getCourseById(id);
        if (existingCourse.getStatus() == CourseStatus.DELETED) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
        if (request.getName() != null
                && courseRepo.existsByName(request.getName())
                && !existingCourse.getName().equals(request.getName())) {
            throw new AppException(ErrorCode.COURSE_EXISTS);
        }
        if (request.getPrice() != null && (request.getPrice() < 10000 || request.getPrice() > 10000000)) {
            throw new AppException(ErrorCode.COURSE_PRICE_INVALID);
        }
        mapper.map(request, existingCourse);
        if(request.getPictureLink() != null){
            fileService.setPicture(request.getPictureLink(),existingCourse);
        }
        existingCourse.setUpdatedBy(accountUtil.getCurrentAccount().getUsername());
        existingCourse.setStatus(CourseStatus.DRAFT);
        existingCourse.setVersion(Math.round((existingCourse.getVersion() + 0.1f) * 10) / 10.0f);
        existingCourse.setUpdatedDate(new Date());

        return courseRepo.save(existingCourse);
    }

    public Page<Course> getCourseByCreatedBy(int offset, int pageSize, String sortBy) {
        String username = accountUtil.getCurrentAccount().getUsername();
        pageUtil.checkOffset(offset);
        Pageable pageable = pageUtil.getPageable(sortBy, offset - 1, pageSize);
        Page<Course> courses = courseRepo.findCourseByCreatedBy(username, pageable);
        if (courses.isEmpty()) {
            return new PageImpl<>(Collections.emptyList());
        }
        return courses;
    }

    public Course approveCourseById(Long id, CourseStatus status) {
        if (status.equals(CourseStatus.PUBLISHED)) {
            Course course = getCourseById(id);
            course.setStatus(CourseStatus.PUBLISHED);
            course.setUpdatedBy(accountUtil.getCurrentAccount().getUsername());
            course.setUpdatedDate(new Date());
            return courseRepo.save(course);

        }
        if (status.equals(CourseStatus.REJECTED)) {
            Course course = getCourseById(id);
            course.setStatus(CourseStatus.REJECTED);
            course.setUpdatedBy(accountUtil.getCurrentAccount().getUsername());
            course.setUpdatedDate(new Date());
            return courseRepo.save(course);
        }
        return null;
    }

    public Course getCourseById(Long id) {
        return courseRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
    }

    public Page<Course> getCourseByStatus(CourseStatus status, int offset, int pageSize, String sortBy) {
        pageUtil.checkOffset(offset);
        Pageable pageable = pageUtil.getPageable(sortBy, offset - 1, pageSize);
        Page<Course> courses = courseRepo.findCourseByStatus(status, pageable);
        if (courses.isEmpty()) {
            return new PageImpl<>(Collections.emptyList());
        }
        return courses;
    }

    public Account addStudiedLesson(Long lessonId) {
        Account account = accountUtil.getCurrentAccount();
        Long chapterId = lessonService.findLessonById(lessonId).getChapter().getId();
        Long courseId = lessonService.findLessonById(lessonId).getChapter().getCourse().getId();
        List<StudiedCourse> studiedCourses = getStudiedCourses(account);
        boolean courseExists = false;

        for (StudiedCourse sc : studiedCourses) {
            if (sc.getCourseId().equals(courseId) && sc.getChapterId().equals(chapterId) && sc.getLessonId().equals(lessonId)) {
                courseExists = true;
                sc.setCheckPoint(true);
            } else {
                sc.setCheckPoint(false);
            }
        }
        if (!courseExists) {
            StudiedCourse newCourse = new StudiedCourse();
            newCourse.setCourseId(courseId);
            newCourse.setChapterId(chapterId);
            newCourse.setLessonId(lessonId);
            newCourse.setCheckPoint(true);
            studiedCourses.add(newCourse);
        }
        saveStudiedCourses(account, studiedCourses);
        // Set Res account
        Account newAccount = new Account();
        newAccount.setUsername(account.getUsername());
        newAccount.setStudiedCourse(account.getStudiedCourse());
        return newAccount;
    }

    public Account addToWishList(List<Long> ids) {
        List<Course> courses = courseRepo.findByIdIn(ids);
        // Check if any IDs are missing
        if (courses.size() != ids.size()) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
        Account account = accountUtil.getCurrentAccount();
        // get old wishlist
        List<Long> wishListCourses = getWishListCourses(account);
        // Add the new course to the wishlist
        List<Long> nonDuplicateIds = ids.stream()
                .filter(id -> !wishListCourses.contains(id))
                .toList();
        wishListCourses.addAll(nonDuplicateIds);
        saveWishListCourses(account, wishListCourses);
        // Return a new account object with the updated wishListCourses
        Account wishListAccount = new Account();
        wishListAccount.setId(account.getId());
        wishListAccount.setWishListCourse(account.getWishListCourse());
        return wishListAccount;
    }

    public Account removeFromWishList(Long id) {
        Account account = accountUtil.getCurrentAccount();
        List<Long> wishListCourses = getWishListCourses(account);
        wishListCourses.remove(id);
        saveWishListCourses(account, wishListCourses);
        // Return a new account object with the updated wishListCourses
        Account wishListAccount = new Account();
        wishListAccount.setId(account.getId());
        wishListAccount.setWishListCourse(account.getWishListCourse());
        return wishListAccount;
    }

    public Page<GeneralCourse> getWishListCourses(int offset, int pageSize, String sortBy) {
        pageUtil.checkOffset(offset);
        Account account = accountUtil.getCurrentAccount();
        List<Long> wishListCourses = getWishListCourses(account);
        Pageable pageable = pageUtil.getPageable(sortBy, offset - 1, pageSize);
        Page<Course> courses = courseRepo.findByIdInAndStatus(wishListCourses, CourseStatus.PUBLISHED, pageable);
        return convertToGeneralCoursePage(courses);
    }

    public Page<GeneralCourse> getCourseByCategory(Category category, int offset, int pageSize, String sortBy) {
        pageUtil.checkOffset(offset);
        Pageable pageable = pageUtil.getPageable(sortBy, offset - 1, pageSize);
        Page<Course> courses;
        if (category == Category.ALL) {
            courses = courseRepo.findAllByStatus(CourseStatus.PUBLISHED, pageable);
        } else {
            courses = courseRepo.findCourseByCategoryAndStatus(category, CourseStatus.PUBLISHED, pageable);
        }
        if (courses.isEmpty()) {
            return new PageImpl<>(new ArrayList<>());
        }
        return convertToGeneralCoursePage(courses);
    }

    public double percentDoneCourse(Long courseId) {
        Account account = accountUtil.getCurrentAccount();
        List<StudiedCourse> studiedCourses = getStudiedCourses(account);

        long completedLessons = studiedCourses.stream()
                .filter(sc -> sc.getCourseId().equals(courseId))
                .count();

        int totalLessons = getTotalLesson(courseId);

        return totalLessons == 0 ? 0 : (double) completedLessons / totalLessons;
    }


    private int getTotalLesson(Long courseId) {
        Course course = getCourseById(courseId);
        return course.getChapter().stream()
                .mapToInt(chapter -> chapter.getLesson().size())
                .sum();
    }

    public Page<GeneralCourse> getAllGeneralCourses(String sortBy, int offset, int pageSize) {
        pageUtil.checkOffset(offset);
        Pageable pageable = pageUtil.getPageable(sortBy, offset - 1, pageSize);
        Page<Course> courses = courseRepo.findCourseByStatus(CourseStatus.PUBLISHED, pageable);
        return convertToGeneralCoursePage(courses);
    }

    public Page<GeneralCourse> getGeneralEnrolledCourses(String sortBy, int offset, int pageSize) {
        pageUtil.checkOffset(offset);
        Page<Course> courses = getEnrolledCoursesPage(offset, pageSize);
        Pageable pageable = pageUtil.getPageable(sortBy, offset - 1, pageSize);
        return convertToGeneralCoursePage(new PageImpl<>(courses.getContent(), pageable, courses.getTotalElements()));
    }

    public Page<Course> getDetailEnrolledCourses(String sortBy, int offset, int pageSize) {
        pageUtil.checkOffset(offset);
        Page<Course> courses = getEnrolledCoursesPage(offset, pageSize);
        Pageable pageable = pageUtil.getPageable(sortBy, offset - 1, pageSize);
        return new PageImpl<>(courses.getContent(), pageable, courses.getTotalElements());
    }

    public void saveCourse(Course course) {
        courseRepo.save(course);
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

    public Page<GeneralCourse> getGeneralCourseByName(String name, int offset, int pageSize, String sortBy) {
        pageUtil.checkOffset(offset);
        Pageable pageable = pageUtil.getPageable(sortBy, offset - 1, pageSize);
        List<Long> courses = courseRepo.findCourseByNameLike("%" + name + "%").stream().map(Course::getId).toList();
        Page<Course> coursePage = courseRepo.findByIdInAndStatus(courses, CourseStatus.PUBLISHED, pageable);
        return convertToGeneralCoursePage(coursePage);
    }

    private Page<Course> getEnrolledCoursesPage(int offset, int pageSize) {
        pageUtil.checkOffset(offset);
        Account account = accountUtil.getCurrentAccount();
        if (account.getEnrolledCourseJson() == null || account.getEnrolledCourseJson().isEmpty()) {
            return new PageImpl<>(Collections.emptyList());
        }

        List<Long> enrolledCourseIds;
        try {
            enrolledCourseIds = objectMapper.readValue(account.getEnrolledCourseJson(), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(offset - 1, pageSize);
        return courseRepo.findByIdInAndStatus(enrolledCourseIds, CourseStatus.PUBLISHED, pageable);
    }

    public List<StudiedCourse> getAllStudiedCourses() {
        Account account = accountUtil.getCurrentAccount();
        return getStudiedCourses(account);
    }

    public StudiedCourse getCheckPoint() {
        Account account = accountUtil.getCurrentAccount();
        List<StudiedCourse> studiedCourses = getStudiedCourses(account);
        for (StudiedCourse sc : studiedCourses) {
            if (sc.isCheckPoint()) {
                return sc;
            }
        }
        return null;
    }

    public Page<Course> getAllCourse(int offset, int pageSize, String sortBy) {
        List<Course> courses = courseRepo.findAll();
        Pageable pageable = pageUtil.getPageable(sortBy, offset - 1, pageSize);
        return new PageImpl<>(courses, pageable, courses.size());
    }

    private List<StudiedCourse> getStudiedCourses(Account account) {
        if (account.getStudiedCourseJson() == null || account.getStudiedCourseJson().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(account.getStudiedCourseJson(), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.PROCESS_ADD_STUDIED_COURSE_FAIL);
        }
    }

    private List<Long> getWishListCourses(Account account) {
        if (account.getWishListCourseJson() == null || account.getWishListCourseJson().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(account.getWishListCourseJson(), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.PROCESS_ADD_STUDIED_COURSE_FAIL);
        }
    }

    private void saveWishListCourses(Account account, List<Long> wishListCourses) {
        try {
            account.setWishListCourseJson(objectMapper.writeValueAsString(wishListCourses));
            accountService.saveAccount(account);
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
        if (createCourseDto.getPrice() < 10000 || createCourseDto.getPrice() > 10000000) {
            throw new AppException(ErrorCode.COURSE_PRICE_INVALID);
        }

    }
}
