package com.fpt.cursus.service;

import com.fpt.cursus.dto.object.StudiedCourse;
import com.fpt.cursus.dto.request.CreateCourseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.entity.Account;
import org.junit.jupiter.api.Test;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.entity.Feedback;
import com.fpt.cursus.repository.AccountRepo;
import com.fpt.cursus.enums.status.CourseStatus;
import com.fpt.cursus.enums.type.Category;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.CourseRepo;
import com.fpt.cursus.repository.FeedbackRepo;
import com.fpt.cursus.util.AccountUtil;
import com.fpt.cursus.service.impl.CourseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @InjectMocks
    private CourseServiceImpl courseService;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private CourseRepo courseRepo;

    @Mock
    private AccountRepo accountRepo;

    @Mock
    private FeedbackRepo feedbackRepo;

    @Mock
    private AccountUtil accountUtil;

    private Account account;
    private CreateCourseDto createCourseDto;
    private Course course;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mapper = new ObjectMapper();
        createCourseDto = new CreateCourseDto();
        createCourseDto.setName("Test Course");
        createCourseDto.setDescription("Test Description");
        createCourseDto.setPictureLink("Test Link");
        createCourseDto.setPrice(10000);
        createCourseDto.setCategory(Category.CATEGORY_1);

        course = new Course();
        course.setId(1L);
        course.setName("Test Course");
        course.setDescription("Test Description");
        course.setPictureLink("Test Link");
        course.setPrice(10000);
        course.setCategory(Category.CATEGORY_1);
    }

    @Test
    void testCreateCourse_Success() {
        when(courseRepo.existsByName(anyString())).thenReturn(false);
        when(courseRepo.save(any(Course.class))).thenReturn(course);
        when(accountUtil.getCurrentAccount()).thenReturn(new Account());

        Course createdCourse = courseService.createCourse(createCourseDto);

        assertNotNull(createdCourse);
        assertEquals(createCourseDto.getName(), createdCourse.getName());
    }

    @Test
    void testCreateCourse_CourseExists() {
        when(courseRepo.existsByName(anyString())).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> courseService.createCourse(createCourseDto));

        assertEquals(ErrorCode.COURSE_EXISTS, exception.getErrorCode());
    }

    @Test
    void testCreateCourse_InvalidPrice() {
        createCourseDto.setPrice(4000);

        AppException exception = assertThrows(AppException.class, () -> courseService.createCourse(createCourseDto));

        assertEquals(ErrorCode.COURSE_PRICE_INVALID, exception.getErrorCode());
    }

    @Test
    void testDeleteCourseById_Success() {
        when(courseRepo.findCourseById(anyLong())).thenReturn(course);
        when(accountUtil.getCurrentAccount()).thenReturn(new Account());

        courseService.deleteCourseById(1L);

        assertEquals(CourseStatus.DELETED, course.getStatus());
        verify(courseRepo, times(1)).save(course);
    }

    @Test
    void testDeleteCourseById_CourseNotFound() {
        when(courseRepo.findCourseById(anyLong())).thenReturn(null);

        AppException exception = assertThrows(AppException.class, () -> courseService.deleteCourseById(1L));

        assertEquals(ErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testUpdateCourse_Success() {
        when(courseRepo.findCourseById(anyLong())).thenReturn(course);
        when(courseRepo.existsByName(anyString())).thenReturn(false);
        when(accountUtil.getCurrentAccount()).thenReturn(new Account());

        courseService.updateCourse(1L, createCourseDto);

        assertEquals(createCourseDto.getName(), course.getName());
        verify(courseRepo, times(1)).save(course);
    }

    @Test
    void testUpdateCourse_CourseNotFound() {
        when(courseRepo.findCourseById(anyLong())).thenReturn(null);

        AppException exception = assertThrows(AppException.class, () -> courseService.updateCourse(1L, createCourseDto));

        assertEquals(ErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testUpdateCourse_CourseExists() {
        when(courseRepo.findCourseById(anyLong())).thenReturn(course);
        when(courseRepo.existsByName(anyString())).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> courseService.updateCourse(1L, createCourseDto));

        assertEquals(ErrorCode.COURSE_EXISTS, exception.getErrorCode());
    }

    @Test
    void testUpdateCourse_CourseDeleted() {
        Long courseId = 1L;
        Course deletedCourse = new Course();
        deletedCourse.setId(courseId);
        deletedCourse.setStatus(CourseStatus.DELETED);

        when(courseRepo.findCourseById(courseId)).thenReturn(deletedCourse);

        AppException exception = assertThrows(AppException.class, () -> courseService.updateCourse(courseId, createCourseDto));
        assertEquals(ErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());

        verify(courseRepo, times(1)).findCourseById(courseId);
        verifyNoMoreInteractions(courseRepo, accountUtil);
    }

    @Test
    void testUpdateCourse_InvalidPrice() {
        Long courseId = 1L;
        createCourseDto.setPrice(4000);

        Course existingCourse = new Course();
        existingCourse.setId(courseId);
        existingCourse.setStatus(CourseStatus.ACTIVE);

        when(courseRepo.findCourseById(courseId)).thenReturn(existingCourse);
        when(courseRepo.existsByName(createCourseDto.getName())).thenReturn(false);

        AppException exception = assertThrows(AppException.class, () -> courseService.updateCourse(courseId, createCourseDto));
        assertEquals(ErrorCode.COURSE_PRICE_INVALID, exception.getErrorCode());

        verify(courseRepo, times(1)).findCourseById(courseId);
        verify(courseRepo, times(1)).existsByName(createCourseDto.getName());
        verifyNoMoreInteractions(courseRepo, accountUtil);
    }

    @Test
    void testVerifyCourseById_Success() {
        when(courseRepo.findCourseById(anyLong())).thenReturn(course);

        courseService.verifyCourseById(1L);

        assertEquals(CourseStatus.PUBLISHED, course.getStatus());
        verify(courseRepo, times(1)).save(course);
    }

    @Test
    void testVerifyCourseById_CourseNotFound() {
        when(courseRepo.findCourseById(anyLong())).thenReturn(null);

        AppException exception = assertThrows(AppException.class, () -> courseService.verifyCourseById(1L));

        assertEquals(ErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testFindCourseById_Success() {
        when(courseRepo.findCourseById(anyLong())).thenReturn(course);

        Course foundCourse = courseService.findCourseById(1L);

        assertNotNull(foundCourse);
        assertEquals(course.getName(), foundCourse.getName());
    }

    @Test
    void testFindCourseById_CourseNotFound() {
        when(courseRepo.findCourseById(anyLong())).thenReturn(null);

        AppException exception = assertThrows(AppException.class, () -> courseService.findCourseById(1L));

        assertEquals(ErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testFindCourseByStatus_Success() {
        List<Course> courses = new ArrayList<>();
        courses.add(course);
        when(courseRepo.findCourseByStatus(any(CourseStatus.class))).thenReturn(courses);

        List<Course> foundCourses = courseService.findCourseByStatus(CourseStatus.PUBLISHED);

        assertNotNull(foundCourses);
        assertEquals(1, foundCourses.size());
    }

    @Test
    void testFindCourseByStatus_CourseNotFound() {
        when(courseRepo.findCourseByStatus(any(CourseStatus.class))).thenReturn(null);

        AppException exception = assertThrows(AppException.class, () -> courseService.findCourseByStatus(CourseStatus.PUBLISHED));

        assertEquals(ErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testGetAllCourse_Success() {
        List<Course> courses = new ArrayList<>();
        courses.add(course);
        Page<Course> coursePage = new PageImpl<>(courses);
        when(courseRepo.findAllByStatus(anyString(), any(PageRequest.class))).thenReturn(coursePage);

        Page<Course> foundCourses = courseService.getAllCourse("name", 0, 10);

        assertNotNull(foundCourses);
        assertEquals(1, foundCourses.getContent().size());
    }

    @Test
    void testGetAllCourse_SortByNull() {
        List<Course> courses = new ArrayList<>();
        courses.add(new Course());
        courses.add(new Course());
        Page<Course> expectedPage = new PageImpl<>(courses);

        int offset = 1;
        int pageSize = 10;
        when(courseRepo.findAllByStatus("PUBLISHED", PageRequest.of(0, pageSize))).thenReturn(expectedPage);

        Page<Course> result = courseService.getAllCourse(null, offset, pageSize);

        verify(courseRepo, times(1)).findAllByStatus("PUBLISHED", PageRequest.of(0, pageSize));
        verifyNoMoreInteractions(courseRepo);

        // Assert the result
        assertEquals(expectedPage, result);
    }

    @Test
    void testGetAllCourse_SortByNotNull() {
        List<Course> courses = new ArrayList<>();
        courses.add(new Course());
        courses.add(new Course());
        Page<Course> expectedPage = new PageImpl<>(courses);

        String sortBy = "name";
        int offset = 1;
        int pageSize = 10;
        when(courseRepo.findAllByStatus("PUBLISHED", PageRequest.of(offset, pageSize).withSort(Sort.by(sortBy))))
                .thenReturn(expectedPage);

        Page<Course> result = courseService.getAllCourse(sortBy, offset, pageSize);

        verify(courseRepo, times(1)).findAllByStatus("PUBLISHED", PageRequest.of(offset, pageSize).withSort(Sort.by(sortBy)));
        verifyNoMoreInteractions(courseRepo);

        assertEquals(expectedPage, result);
    }

    @Test
    void testGetEnrolledCourses_Success() {
        Account account = new Account();
        account.setEnrolledCourseJson("[1]");
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(courseRepo.findByIdIn(anyList())).thenReturn(List.of(course));

        List<Course> enrolledCourses = courseService.getEnrolledCourses();

        assertNotNull(enrolledCourses);
        assertEquals(1, enrolledCourses.size());
    }

    @Test
    void testGetEnrolledCourses_UserNotFound() {
        when(accountUtil.getCurrentAccount()).thenReturn(null);

        AppException exception = assertThrows(AppException.class, () -> courseService.getEnrolledCourses());

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testGetEnrolledCourses_EnrolledCoursesEmpty() {
        Account account = new Account();
        account.setEnrolledCourseJson(null);
        when(accountUtil.getCurrentAccount()).thenReturn(account);

        AppException exception = assertThrows(AppException.class, () -> courseService.getEnrolledCourses());

        assertEquals(ErrorCode.USER_ENROLLED_EMPTY, exception.getErrorCode());
    }

    @Test
    void testGetEnrolledCourses_AccountNotFound() {
        when(accountUtil.getCurrentAccount()).thenReturn(null);

        AppException exception = org.junit.jupiter.api.Assertions.assertThrows(AppException.class, () -> {
            courseService.getEnrolledCourses();
        });

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testAddStudiedLesson_Success(){
        Long courseId = 1L;
        Long lessonId = 1L;
        Account account = new Account();
        account.setStudiedCourse(null);
        when(accountUtil.getCurrentAccount()).thenReturn(account);

        courseService.addStudiedLesson(courseId, lessonId);

        verify(accountRepo, times(1)).save(account);
        List<StudiedCourse> studiedCourses = account.getStudiedCourse();
        assert studiedCourses != null;
        assert studiedCourses.size() == 1;
        assert studiedCourses.get(0).getId().equals(courseId);
        assert studiedCourses.get(0).getLessonIds().size() == 1;
        assert studiedCourses.get(0).getLessonIds().get(0).equals(lessonId);
    }

    @Test
    void testAddStudiedLesson_StudiedCourseIsNull(){
        Long courseId = 1L;
        Long lessonId = 2L;

        Account account = new Account();
        account.setStudiedCourse(null);

        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(accountRepo.save(any(Account.class))).thenAnswer(invocation -> {
            Account savedAccount = invocation.getArgument(0);
            assertNotNull(savedAccount.getStudiedCourse());
            assertEquals(1, savedAccount.getStudiedCourse().size());

            StudiedCourse savedCourse = savedAccount.getStudiedCourse().get(0);
            assertEquals(courseId, savedCourse.getId());
            assertTrue(savedCourse.getLessonIds().contains(lessonId));
            return savedAccount;
        });

        courseService.addStudiedLesson(courseId, lessonId);

        verify(accountRepo, times(1)).save(any(Account.class));
    }

    @Test
    void testAddStudiedLesson_ExistingStudiedCourse(){
        Long courseId = 1L;
        Long lessonId1 = 2L;
        Long lessonId2 = 3L;

        StudiedCourse existingCourse = new StudiedCourse();
        existingCourse.setId(courseId);
        existingCourse.setLessonIds(new ArrayList<>(List.of(lessonId1)));
        existingCourse.setLessonIds(new ArrayList<>(List.of(lessonId2)));

        Account account = new Account();
        account.setStudiedCourse(new ArrayList<>(List.of(existingCourse)));
        account.setStudiedCourseJson("[1, 2]");

        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(accountRepo.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        courseService.addStudiedLesson(courseId, lessonId1);
        courseService.addStudiedLesson(courseId, lessonId2);

        verify(accountRepo, times(1)).save(any(Account.class));
        assertEquals(1, account.getStudiedCourse().size());
        StudiedCourse updatedCourse = account.getStudiedCourse().get(0);
        assertEquals(courseId, updatedCourse.getId());
        assertTrue(updatedCourse.getLessonIds().contains(lessonId1));
        assertTrue(updatedCourse.getLessonIds().contains(lessonId2));
    }

    @Test
    void testAddStudiedLesson_Success_NoExistingStudiedCourse() {
        Long courseId = 1L;
        Long lessonId = 1L;
        Account account = new Account();
        account.setStudiedCourse(null); // Simulate no existing studied courses
        when(accountUtil.getCurrentAccount()).thenReturn(account);

        courseService.addStudiedLesson(courseId, lessonId);

        verify(accountRepo, times(1)).save(any(Account.class));
        assertNotNull(account.getStudiedCourse());
        assertEquals(1, account.getStudiedCourse().size());
        assertEquals(courseId, account.getStudiedCourse().get(0).getId());
        assertTrue(account.getStudiedCourse().get(0).getLessonIds().contains(lessonId));
    }

    @Test
    void testGetEnrolledCourses_EmptyEnrolledCourseJson() {
        Account account = new Account();
        account.setEnrolledCourseJson("");

        when(accountUtil.getCurrentAccount()).thenReturn(account);

        AppException exception = org.junit.jupiter.api.Assertions.assertThrows(AppException.class, () -> {
            courseService.getEnrolledCourses();
        });

        assertEquals(ErrorCode.USER_ENROLLED_EMPTY, exception.getErrorCode());
    }

    @Test
    void testGetEnrolledCourses_JsonProcessingException() {
        Account account = new Account();
        account.setEnrolledCourseJson("invalid_json");

        when(accountUtil.getCurrentAccount()).thenReturn(account);

        AppException exception = org.junit.jupiter.api.Assertions.assertThrows(AppException.class, () -> {
            courseService.getEnrolledCourses();
        });

        assertEquals(ErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testPercentDoneCourse_CourseFoundStudiedCourseFound() throws JsonProcessingException {
        Long courseId = 1L;
        Long lessonId = 2L;

        Course course = new Course();
        course.setId(courseId);
        when(courseRepo.findCourseById(courseId)).thenReturn(course);

        List<StudiedCourse> studiedCourses = new ArrayList<>();
        StudiedCourse studiedCourse = new StudiedCourse();
        studiedCourse.setId(courseId);
        studiedCourse.setLessonIds(new ArrayList<>(List.of(1L, lessonId)));
        studiedCourses.add(studiedCourse);

        Account account = new Account();
        account.setStudiedCourseJson(mapper.writeValueAsString(studiedCourses));
        when(accountUtil.getCurrentAccount()).thenReturn(account);

        //when(courseService.getTotalLesson(courseId)).thenReturn(3);

        double percentDone = courseService.percentDoneCourse(courseId);

        verify(courseRepo, times(1)).findCourseById(courseId);
        verify(accountUtil, times(1)).getCurrentAccount();
        verifyNoMoreInteractions(courseRepo, accountUtil);

        assertEquals(2.0 / 3.0, percentDone);
    }

    @Test
    void testPercentDoneCourse_CourseNotFound() {
        Long courseId = 1L;

        when(courseRepo.findCourseById(courseId)).thenReturn(null);

        AppException exception = assertThrows(AppException.class, () -> courseService.percentDoneCourse(courseId));

        assertEquals(ErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());

        verify(courseRepo, times(1)).findCourseById(courseId);
        verifyNoMoreInteractions(courseRepo, accountUtil);
    }

    @Test
    void testPercentDoneCourse_StudiedCourseNotFound(){
        Long courseId = 1L;

        Course course = new Course();
        course.setId(courseId);
        when(courseRepo.findCourseById(courseId)).thenReturn(course);

        Account account = new Account();
        account.setStudiedCourseJson("[]");
        when(accountUtil.getCurrentAccount()).thenReturn(account);

        double percentDone = courseService.percentDoneCourse(courseId);

        verify(courseRepo, times(1)).findCourseById(courseId);
        verify(accountUtil, times(1)).getCurrentAccount();
        verifyNoMoreInteractions(courseRepo, accountUtil);

        assertEquals(0.0, percentDone);
    }

    @Test
    void testRatingCourse() {
        long courseId = 1L;
        float ratingToAdd = 4.5f;

        List<Feedback> feedbacks = new ArrayList<>();
        feedbacks.add(new Feedback());
        when(feedbackRepo.findFeedbackByCourseId(courseId)).thenReturn(feedbacks);

        Course course = new Course();
        course.setId(courseId);
        course.setRating(5.0f);
        when(courseRepo.findCourseById(courseId)).thenReturn(course);

        courseService.ratingCourse(courseId, ratingToAdd);

        verify(feedbackRepo, times(1)).findFeedbackByCourseId(courseId);
        verify(courseRepo, times(1)).findCourseById(courseId);
        verify(courseRepo, times(1)).save(course);

        float expectedRating = (5.0f * feedbacks.size() + ratingToAdd) / (feedbacks.size() + 1);
        assertEquals(expectedRating, course.getRating(), 10.); // Assert with a delta for floating point comparison
    }
}

