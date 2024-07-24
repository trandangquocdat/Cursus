package com.fpt.cursus.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.dto.object.StudiedCourse;
import com.fpt.cursus.dto.request.CreateCourseDto;
import com.fpt.cursus.dto.request.UpdateCourseDto;
import com.fpt.cursus.dto.response.CustomAccountResDto;
import com.fpt.cursus.dto.response.GeneralCourse;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.enums.Category;
import com.fpt.cursus.enums.CourseStatus;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.CourseRepo;
import com.fpt.cursus.service.AccountService;
import com.fpt.cursus.service.CourseService;
import com.fpt.cursus.service.FileService;
import com.fpt.cursus.service.LessonService;
import com.fpt.cursus.util.AccountUtil;
import com.fpt.cursus.util.FileUtil;
import com.fpt.cursus.util.PageUtil;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepo courseRepo;
    private final AccountUtil accountUtil;
    private final AccountService accountService;
    private final ObjectMapper objectMapper;
    private final LessonService lessonService;
    private final PageUtil pageUtil;
    private final ModelMapper modelMapper;
    private final FileService fileService;
    private final FileUtil fileUtil;

    @Autowired
    public CourseServiceImpl(AccountUtil accountUtil,
                             AccountService accountService,
                             CourseRepo courseRepo,
                             ObjectMapper objectMapper,
                             LessonService lessonService,
                             PageUtil pageUtil,
                             ModelMapper modelMapper,
                             FileService fileService,
                             FileUtil fileUtil) {
        this.accountUtil = accountUtil;
        this.accountService = accountService;
        this.courseRepo = courseRepo;
        this.objectMapper = objectMapper;
        this.lessonService = lessonService;
        this.pageUtil = pageUtil;
        this.modelMapper = modelMapper;
        this.fileService = fileService;
        this.fileUtil = fileUtil;
    }

    @Override
    public Course createCourse(CreateCourseDto createCourseDto) {
        validateCourseDto(createCourseDto);
        Course course = modelMapper.map(createCourseDto, Course.class);
        String folder = accountUtil.getCurrentAccount().getUsername();
        Date date = new Date();
        course.setCreatedDate(date);
        course.setCreatedBy(accountUtil.getCurrentAccount().getUsername());
        if (createCourseDto.getPictureLink() == null) {
            course.setPictureLink("defaultImage.jpg");
        } else if (!fileUtil.isImage(createCourseDto.getPictureLink())) {
            throw new AppException(ErrorCode.FILE_INVALID_IMAGE);
        } else {
            String link = fileService.linkSave(createCourseDto.getPictureLink(), folder);
            course.setPictureLink(link);
        }
        course.setStatus(CourseStatus.DRAFT);
        course.setVersion(1f);
        return courseRepo.save(course);

    }

    @Override
    public Course deleteCourseById(Long id) {
        Course course = getCourseById(id);
        course.setUpdatedBy(accountUtil.getCurrentAccount().getUsername());
        course.setUpdatedDate(new Date());
        course.setStatus(CourseStatus.DELETED);
        return courseRepo.save(course);
    }

    @Override
    public Course updateCourse(Long id, UpdateCourseDto request) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT).setSkipNullEnabled(true);
        String folder = accountUtil.getCurrentAccount().getUsername();
        Course existingCourse = getCourseById(id);
        if (existingCourse.getStatus() == CourseStatus.DELETED) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
        if (request.getName() != null && courseRepo.existsByName(request.getName()) && !existingCourse.getName().equals(request.getName())) {
            throw new AppException(ErrorCode.COURSE_EXISTS);
        }
        if (request.getPrice() != null && (request.getPrice() < 10000 || request.getPrice() > 10000000)) {
            throw new AppException(ErrorCode.COURSE_PRICE_INVALID);
        }
        mapper.map(request, existingCourse);
        if (request.getPictureLink() != null) {
            String link = fileService.linkSave(request.getPictureLink(), folder);
            existingCourse.setPictureLink(link);
        }
        existingCourse.setUpdatedBy(accountUtil.getCurrentAccount().getUsername());
        existingCourse.setStatus(CourseStatus.DRAFT);
        existingCourse.setVersion(Math.round((existingCourse.getVersion() + 0.1f) * 10) / 10.0f);
        existingCourse.setUpdatedDate(new Date());
        return courseRepo.save(existingCourse);
    }

    @Override
    public Page<Course> getCourseByCreatedBy(int offset, int pageSize, String sortBy) {
        String username = accountUtil.getCurrentAccount().getUsername();
        pageUtil.checkOffset(offset);
        Pageable pageable = pageUtil.getPageable(sortBy, offset - 1, pageSize);
        Page<Course> courses = courseRepo.findCourseByCreatedBy(username, pageable);
        for (Course course : courses) {
            course.setPictureLink(fileService.getSignedImageUrl(course.getPictureLink()));
        }
        if (courses.isEmpty()) {
            return new PageImpl<>(Collections.emptyList());
        }
        return courses;
    }

    @Override
    public List<Course> getCourseByCreatedBy(String username) {
        List<Course> courses = courseRepo.findCourseByCreatedBy(username);
        for (Course course : courses) {
            course.setPictureLink(fileService.getSignedImageUrl(course.getPictureLink()));
        }
        if (courses.isEmpty()) {
            return new ArrayList<>();
        }
        return courses;
    }

    @Override
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

    @Override
    public Course getCourseById(Long id) {
        return courseRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
    }

    @Override
    public Page<Course> getCourseByStatus(CourseStatus status, int offset, int pageSize, String sortBy) {
        pageUtil.checkOffset(offset);
        Pageable pageable = pageUtil.getPageable(sortBy, offset - 1, pageSize);
        Page<Course> courses = courseRepo.findCourseByStatus(status, pageable);
        for (Course course : courses) {
            course.setPictureLink(fileService.getSignedImageUrl(course.getPictureLink()));
        }
        if (courses.isEmpty()) {
            return new PageImpl<>(Collections.emptyList());
        }
        return courses;
    }

    @Override
    public CustomAccountResDto addStudiedLesson(Long lessonId) {
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
        CustomAccountResDto newAccount = new CustomAccountResDto();
        newAccount.setId(account.getId());
        newAccount.setStudiedCourses(studiedCourses);
        return newAccount;
    }

    @Override
    public CustomAccountResDto addToWishList(List<Long> ids) {
        List<Course> courses = courseRepo.findByIdIn(ids);
        // Check if any IDs are missing
        if (courses.size() != ids.size()) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
        Account account = accountUtil.getCurrentAccount();
        // get old wishlist
        List<Long> wishListCourses = getWishListCourses(account);
        // Add the new course to the wishlist
        List<Long> nonDuplicateIds = ids.stream().filter(id -> !wishListCourses.contains(id)).toList();
        wishListCourses.addAll(nonDuplicateIds);
        saveWishListCourses(account, wishListCourses);
        // Return a new account object with the updated wishListCourses
        CustomAccountResDto wishListAccount = new CustomAccountResDto();
        wishListAccount.setId(account.getId());
        wishListAccount.setWishListCourses(wishListCourses);
        return wishListAccount;
    }

    @Override
    public CustomAccountResDto removeFromWishList(Long id) {
        Account account = accountUtil.getCurrentAccount();
        List<Long> wishListCourses = getWishListCourses(account);
        wishListCourses.remove(id);
        saveWishListCourses(account, wishListCourses);
        // Return a new account object with the updated wishListCourses
        CustomAccountResDto wishListAccount = new CustomAccountResDto();
        wishListAccount.setId(account.getId());
        wishListAccount.setWishListCourses(wishListCourses);
        return wishListAccount;
    }

    @Override
    public Page<GeneralCourse> getWishListCourses(int offset, int pageSize, String sortBy) {
        pageUtil.checkOffset(offset);
        Account account = accountUtil.getCurrentAccount();
        List<Long> wishListCourses = getWishListCourses(account);
        Pageable pageable = pageUtil.getPageable(sortBy, offset - 1, pageSize);
        Page<Course> courses = courseRepo.findByIdInAndStatus(wishListCourses, CourseStatus.PUBLISHED, pageable);
        return convertToGeneralCoursePage(courses);
    }

    @Override
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
        } else {
            for (Course course : courses) {
                course.setPictureLink(fileService.getSignedImageUrl(course.getPictureLink()));
            }
        }
        return convertToGeneralCoursePage(courses);
    }

    @Override
    public double percentDoneCourse(Long courseId) {
        Account account = accountUtil.getCurrentAccount();
        List<StudiedCourse> studiedCourses = getStudiedCourses(account);

        long completedLessons = studiedCourses.stream().filter(sc -> sc.getCourseId().equals(courseId)).count();

        int totalLessons = getTotalLesson(courseId);

        return totalLessons == 0 ? 0 : (double) completedLessons / totalLessons;
    }


    private int getTotalLesson(Long courseId) {
        Course course = getCourseById(courseId);
        return course.getChapter().stream().mapToInt(chapter -> chapter.getLesson().size()).sum();
    }

    @Override
    public Page<GeneralCourse> getAllGeneralCourses(String sortBy, int offset, int pageSize) {
        pageUtil.checkOffset(offset);
        Pageable pageable = pageUtil.getPageable(sortBy, offset - 1, pageSize);
        Page<Course> courses = courseRepo.findCourseByStatus(CourseStatus.PUBLISHED, pageable);
        for (Course course : courses) {
            course.setPictureLink(fileService.getSignedImageUrl(course.getPictureLink()));
        }
        return convertToGeneralCoursePage(courses);
    }

    @Override
    public Page<GeneralCourse> getGeneralEnrolledCourses(String sortBy, int offset, int pageSize) {
        pageUtil.checkOffset(offset);
        Page<Course> courses = getEnrolledCoursesPage(offset, pageSize);
        for (Course course : courses) {
            course.setPictureLink(fileService.getSignedImageUrl(course.getPictureLink()));
        }
        Pageable pageable = pageUtil.getPageable(sortBy, offset - 1, pageSize);
        return convertToGeneralCoursePage(new PageImpl<>(courses.getContent(), pageable, courses.getTotalElements()));
    }

    @Override
    public Page<Course> getDetailEnrolledCourses(String sortBy, int offset, int pageSize) {
        pageUtil.checkOffset(offset);
        Page<Course> courses = getEnrolledCoursesPage(offset, pageSize);
        for (Course course : courses) {
            course.setPictureLink(fileService.getSignedImageUrl(course.getPictureLink()));
        }
        Pageable pageable = pageUtil.getPageable(sortBy, offset - 1, pageSize);
        return new PageImpl<>(courses.getContent(), pageable, courses.getTotalElements());
    }

    @Override
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
        generalCourse.setDescription(course.getDescription());
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

    @Override
    public Page<GeneralCourse> getGeneralCourseByName(String name, int offset, int pageSize, String sortBy) {
        pageUtil.checkOffset(offset);
        Pageable pageable = pageUtil.getPageable(sortBy, offset - 1, pageSize);
        List<Long> courses = courseRepo.findCourseByNameLike("%" + name + "%").stream().map(Course::getId).toList();
        Page<Course> coursePage = courseRepo.findByIdInAndStatus(courses, CourseStatus.PUBLISHED, pageable);
        for (Course course : coursePage) {
            course.setPictureLink(fileService.getSignedImageUrl(course.getPictureLink()));
        }
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
        Page<Course> coursePage = courseRepo.findByIdInAndStatus(enrolledCourseIds, CourseStatus.PUBLISHED, pageable);
        for (Course course : coursePage) {
            course.setPictureLink(fileService.getSignedImageUrl(course.getPictureLink()));
        }
        return coursePage;
    }

    @Override
    public List<StudiedCourse> getAllStudiedCourses() {
        Account account = accountUtil.getCurrentAccount();
        return getStudiedCourses(account);
    }

    @Override
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

    @Override
    public Page<Course> getAllCourse(int offset, int pageSize, String sortBy) {
        List<Course> courses = courseRepo.findAll();
        for (Course course : courses) {
            course.setPictureLink(fileService.getSignedImageUrl(course.getPictureLink()));
        }
        Pageable pageable = pageUtil.getPageable(sortBy, offset - 1, pageSize);
        return new PageImpl<>(courses, pageable, courses.size());
    }

    @Override
    public List<Course> getCourseByIdsIn(List<Long> courseIds) {
        List<Course> courses = courseRepo.findByIdIn(courseIds);
        if (courses.isEmpty()) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
        return courses;
    }

    public List<StudiedCourse> getStudiedCourses(Account account) {
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
