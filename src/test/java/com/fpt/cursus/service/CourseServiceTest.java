package com.fpt.cursus.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.dto.object.StudiedCourse;
import com.fpt.cursus.dto.request.CreateCourseDto;
import com.fpt.cursus.dto.request.UpdateCourseDto;
import com.fpt.cursus.dto.response.CustomAccountResDto;
import com.fpt.cursus.dto.response.GeneralCourse;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Chapter;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.entity.Lesson;
import com.fpt.cursus.enums.Category;
import com.fpt.cursus.enums.CourseStatus;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.CourseRepo;
import com.fpt.cursus.service.impl.AccountServiceImpl;
import com.fpt.cursus.service.impl.CourseServiceImpl;
import com.fpt.cursus.util.AccountUtil;
import com.fpt.cursus.util.FileUtil;
import com.fpt.cursus.util.PageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @Mock
    private CourseRepo courseRepo;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private FileUtil fileUtil;

    @Mock
    private FileService fileService;

    @Mock
    private PageUtil pageUtil;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CourseServiceImpl courseServiceImpl;

    @Mock
    private LessonService lessonService;

    @Mock
    private AccountUtil accountUtil;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private CourseServiceImpl courseService;

    private List<Course> courses;
    private Course sampleCourse;
    private CreateCourseDto sampleCreateCourseDto;
    private UpdateCourseDto sampleUpdateCourseDto;
    private Account account;
    private Lesson lesson;
    private Chapter chapter;
    private Course course;
    private List<StudiedCourse> studiedCourses;


    @BeforeEach
    void setUp() {
        account = new Account();
        account.setUsername("testuser");
        course = new Course();
        course.setId(1L);
        chapter = new Chapter();
        chapter.setId(1L);
        chapter.setCourse(course);
        lesson = new Lesson();
        lesson.setId(1L);
        lesson.setChapter(chapter);
        studiedCourses = new ArrayList<>();
        courses = List.of(new Course(), new Course());

        sampleCourse = new Course();
        sampleCourse.setId(1L);
        sampleCourse.setName("Sample Course");
        sampleCourse.setPrice(500000);
        sampleCourse.setStatus(CourseStatus.DRAFT);
        sampleCourse.setStatus(CourseStatus.ACTIVE);

        sampleCreateCourseDto = new CreateCourseDto();
        sampleCreateCourseDto.setName("New Course");
        sampleCreateCourseDto.setPrice(200000.0);
        sampleCreateCourseDto.setPictureLink(null);

        sampleUpdateCourseDto = new UpdateCourseDto();
        sampleUpdateCourseDto.setName("Updated Course");
        sampleUpdateCourseDto.setPrice(300000.0);
        sampleUpdateCourseDto.setPictureLink(null);
    }

    @Test
    void testCreateCourse_ValidDto() {
        Course mockMappedCourse = new Course();
        mockMappedCourse.setName(sampleCreateCourseDto.getName());
        mockMappedCourse.setPrice(sampleCreateCourseDto.getPrice());

        when(modelMapper.map(sampleCreateCourseDto, Course.class)).thenReturn(mockMappedCourse);
        when(fileUtil.isImage(sampleCreateCourseDto.getPictureLink())).thenReturn(true);

        Course savedCourse = new Course();
        savedCourse.setId(1L);
        savedCourse.setName(sampleCreateCourseDto.getName());
        savedCourse.setPrice(sampleCreateCourseDto.getPrice());
        savedCourse.setCreatedDate(new Date());
        savedCourse.setCreatedBy("testuser");
        savedCourse.setStatus(CourseStatus.DRAFT);
        savedCourse.setVersion(1f);

        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(courseRepo.save(any(Course.class))).thenReturn(savedCourse);

        Course createdCourse = courseService.createCourse(sampleCreateCourseDto);

        assertEquals(savedCourse.getId(), createdCourse.getId());
        assertEquals(savedCourse.getName(), createdCourse.getName());
        assertEquals(savedCourse.getPrice(), createdCourse.getPrice());
        assertEquals(savedCourse.getCreatedBy(), createdCourse.getCreatedBy());
        assertEquals(savedCourse.getStatus(), createdCourse.getStatus());
        assertEquals(savedCourse.getVersion(), createdCourse.getVersion());

        verify(modelMapper, times(1)).map(sampleCreateCourseDto, Course.class);
        verify(fileUtil, times(1)).isImage(sampleCreateCourseDto.getPictureLink());
        verify(fileService, times(1)).setPicture(sampleCreateCourseDto.getPictureLink(), mockMappedCourse);
        verify(courseRepo, times(1)).save(mockMappedCourse);
    }

    @Test
    void testCreateCourse_InvalidImage() {

        sampleCreateCourseDto.setPictureLink(null);
        when(fileUtil.isImage(sampleCreateCourseDto.getPictureLink())).thenReturn(false);
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(modelMapper.map(sampleCreateCourseDto, Course.class)).thenReturn(sampleCourse);

        AppException exception = assertThrows(AppException.class,
                () -> courseService.createCourse(sampleCreateCourseDto));

        assertEquals(ErrorCode.FILE_INVALID_IMAGE, exception.getErrorCode());
    }

    @Test
    void testDeleteCourseById_Success() {
        //Given
        Course course = new Course();
        course.setId(1L);
        course.setStatus(CourseStatus.ACTIVE);
        //When
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(courseRepo.findById(anyLong())).thenReturn(Optional.of(course));
        when(courseRepo.save(any())).thenReturn(course);

        Course deletedCourse = courseService.deleteCourseById(1L);
        //Then
        assertEquals(CourseStatus.DELETED, deletedCourse.getStatus());
        assertEquals("testuser", deletedCourse.getUpdatedBy());
        assertEquals(sampleCourse.getId(), deletedCourse.getId());

    }

    @Test
    void testDeleteCourseById_CourseNotFound() {
        //When
        when(courseRepo.findById(1L)).thenReturn(Optional.empty());

        // Then
        AppException exception = assertThrows(AppException.class,
                () -> courseService.deleteCourseById(1L));
        assertEquals(ErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());

        // Verify interactions
        verify(courseRepo, times(1)).findById(1L);
        verify(courseRepo, never()).save(any(Course.class));
    }

    @Test
    void testUpdateCourse_CourseNotFound() {
        //when
        when(courseRepo.findById(1L)).thenReturn(java.util.Optional.empty());

        //Then
        AppException exception = assertThrows(AppException.class,
                () -> courseService.updateCourse(1L, sampleUpdateCourseDto));
        assertEquals(ErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());
        verify(courseRepo, never()).save(any());
    }

    @Test
    void testUpdateCourse_Success() {
        //Given
        Long courseId = 1L;
        Course existingCourse = new Course();
        existingCourse.setId(courseId);
        existingCourse.setName("Existing Course");
        existingCourse.setPrice(50000);
        existingCourse.setPictureLink("existing_link.jpg");
        existingCourse.setStatus(CourseStatus.ACTIVE);
        existingCourse.setVersion(1.0f);

        //When
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(courseRepo.findById(courseId)).thenReturn(Optional.of(existingCourse));
        when(courseRepo.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateCourseDto updateDto = new UpdateCourseDto();
        updateDto.setName("Updated Course");
        updateDto.setPrice(60000.0);
        updateDto.setPictureLink(null);

        Course updatedCourse = courseService.updateCourse(courseId, updateDto);

        //Then
        assertEquals(updateDto.getName(), updatedCourse.getName());
        assertEquals(updateDto.getPrice(), updatedCourse.getPrice());

        assertEquals(CourseStatus.DRAFT, updatedCourse.getStatus());
        assertEquals("testuser", updatedCourse.getUpdatedBy());

        assertNotNull(updatedCourse.getUpdatedDate());

        verify(courseRepo, times(1)).findById(courseId);
        verify(courseRepo, times(1)).save(existingCourse);
    }

    @Test
    void testGetCourseById_Success() {
        //When
        when(courseRepo.findById(1L)).thenReturn(java.util.Optional.ofNullable(sampleCourse));

        // Test
        Course foundCourse = courseService.getCourseById(1L);

        //Then
        assertNotNull(foundCourse);
        assertEquals(sampleCourse.getName(), foundCourse.getName());
        assertEquals(sampleCourse.getPrice(), foundCourse.getPrice());
        assertEquals(sampleCourse.getStatus(), foundCourse.getStatus());
    }

    @Test
    void testGetCourseById_CourseNotFound() {
        //When
        when(courseRepo.findById(1L)).thenReturn(java.util.Optional.empty());

        //Then
        AppException exception = assertThrows(AppException.class, () -> courseService.getCourseById(1L));
        assertEquals(ErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testUpdateCourse_CourseDeleted() {
        //Given
        Long courseId = 1L;
        Course existingCourse = new Course();
        existingCourse.setId(courseId);
        existingCourse.setStatus(CourseStatus.DELETED);

        //WHen
        when(courseRepo.findById(courseId)).thenReturn(Optional.of(existingCourse));

        // Create update DTO
        UpdateCourseDto updateDto = new UpdateCourseDto();
        updateDto.setName("Updated Course");
        updateDto.setPrice(60000.0);
        updateDto.setPictureLink(null);

        // Call update method and expect AppException
        AppException exception = assertThrows(AppException.class, () -> {
            courseService.updateCourse(courseId, updateDto);
        });

        //Then
        assertEquals(ErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());

        verify(courseRepo, times(1)).findById(courseId);
        verify(courseRepo, never()).save(any());
    }

    @Test
    void testUpdateCourse_NameAlreadyExists() {
        //Given
        Long courseId = 1L;
        Course existingCourse = new Course();
        existingCourse.setId(courseId);
        existingCourse.setName("Existing Course");
        existingCourse.setStatus(CourseStatus.DRAFT);

        //When
        when(courseRepo.findById(courseId)).thenReturn(Optional.of(existingCourse));

        String newCourseName = "New Course";
        when(courseRepo.existsByName(newCourseName)).thenReturn(true);

        // Create update DTO with new name
        UpdateCourseDto updateDto = new UpdateCourseDto();
        updateDto.setName(newCourseName);
        updateDto.setPrice(60000.0);
        updateDto.setPictureLink(null);

        //Then
        AppException exception = assertThrows(AppException.class, () -> {
            courseService.updateCourse(courseId, updateDto);
        });

        assertEquals(ErrorCode.COURSE_EXISTS, exception.getErrorCode());

        verify(courseRepo, times(1)).findById(courseId);
        verify(courseRepo, times(1)).existsByName(newCourseName);
        verify(courseRepo, never()).save(any());
    }

    @Test
    void testUpdateCourse_InvalidPriceTooLow() {
        //Given
        Long courseId = 1L;
        Course existingCourse = new Course();
        existingCourse.setId(courseId);
        existingCourse.setName("Existing Course");
        existingCourse.setStatus(CourseStatus.DRAFT);

        //When
        when(courseRepo.findById(courseId)).thenReturn(Optional.of(existingCourse));

        UpdateCourseDto updateDto = new UpdateCourseDto();
        updateDto.setPrice(5000.0);

        // Call update method and expect AppException
        AppException exception = assertThrows(AppException.class, () -> {
            courseService.updateCourse(courseId, updateDto);
        });

        //Then
        assertEquals(ErrorCode.COURSE_PRICE_INVALID, exception.getErrorCode());
        verify(courseRepo, times(1)).findById(courseId);
        verify(courseRepo, never()).save(any());
    }

    @Test
    void testUpdateCourse_InvalidPriceTooHigh() {
        //Given
        Long courseId = 1L;
        Course existingCourse = new Course();
        existingCourse.setId(courseId);
        existingCourse.setName("Existing Course");
        existingCourse.setStatus(CourseStatus.DRAFT);

        //When
        when(courseRepo.findById(courseId)).thenReturn(Optional.of(existingCourse));

        UpdateCourseDto updateDto = new UpdateCourseDto();
        updateDto.setPrice(15000000.0);  // Invalid price

        AppException exception = assertThrows(AppException.class, () -> {
            courseService.updateCourse(courseId, updateDto);
        });

        // THen
        assertEquals(ErrorCode.COURSE_PRICE_INVALID, exception.getErrorCode());
        verify(courseRepo, times(1)).findById(courseId);
        verify(courseRepo, never()).save(any());
    }

    @Test
    void testUpdateCourse_ValidPictureLink() {
        //Given
        Long courseId = 1L;
        Course existingCourse = new Course();
        existingCourse.setId(courseId);
        existingCourse.setName("Existing Course");
        existingCourse.setStatus(CourseStatus.DRAFT);

        //When
        when(courseRepo.findById(courseId)).thenReturn(Optional.of(existingCourse));

        Account sampleAccount = new Account();
        sampleAccount.setUsername("testUser");
        when(accountUtil.getCurrentAccount()).thenReturn(sampleAccount);

        UpdateCourseDto updateDto = new UpdateCourseDto();
        updateDto.setPictureLink(null);

        courseService.updateCourse(courseId, updateDto);

        verify(courseRepo, times(1)).save(existingCourse);

        assertEquals("testUser", existingCourse.getUpdatedBy());
        assertEquals(CourseStatus.DRAFT, existingCourse.getStatus());
    }

    @Test
    void testGetCourseByCreatedBy_NoCoursesFound() {
        String username = "testUser";
        int offset = 1;
        int pageSize = 10;
        String sortBy = "name";

        when(accountUtil.getCurrentAccount()).thenReturn(account);

        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(sortBy));
        when(pageUtil.getPageable(anyString(), anyInt(), anyInt())).thenReturn(pageable);

        when(courseRepo.findCourseByCreatedBy(anyString(), any(Pageable.class))).thenReturn(Page.empty());

        Page<Course> result = courseService.getCourseByCreatedBy(offset, pageSize, sortBy);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCourseByCreatedBy_CoursesFound() {
        String username = "testUser";
        int offset = 1;
        int pageSize = 10;
        String sortBy = "name";

        when(accountUtil.getCurrentAccount()).thenReturn(account);


        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(sortBy));
        when(pageUtil.getPageable(anyString(), anyInt(), anyInt())).thenReturn(pageable);

        Course course1 = new Course();
        course1.setName("Course 1");
        Course course2 = new Course();
        course2.setName("Course 2");
        List<Course> courses = List.of(course1, course2);
        Page<Course> coursePage = new PageImpl<>(courses);

        when(courseRepo.findCourseByCreatedBy(anyString(), any(Pageable.class))).thenReturn(coursePage);

        Page<Course> result = courseService.getCourseByCreatedBy(offset, pageSize, sortBy);

        assertFalse(result.isEmpty());
        assertEquals(2, result.getTotalElements());
        assertEquals("Course 1", result.getContent().get(0).getName());
        assertEquals("Course 2", result.getContent().get(1).getName());
    }
//update
    @Test
    void testApproveCourseById_Published() {
        //Given
        Long courseId = 1L;
        Course course = new Course();
        course.setId(courseId);
        course.setStatus(CourseStatus.DRAFT);
        //When
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(courseRepo.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepo.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));

        Course result = courseService.approveCourseById(courseId, CourseStatus.PUBLISHED);
        //Then
        verify(courseRepo, times(1)).findById(courseId);
        verify(courseRepo, times(1)).save(course);
        assertEquals(CourseStatus.PUBLISHED, result.getStatus());
        assertEquals("testuser", result.getUpdatedBy());
        assertNotNull(result.getUpdatedDate());
    }

    @Test
    void testApproveCourseById_Rejected() {
        //Given
        Long courseId = 1L;
        Course course = new Course();
        course.setId(courseId);
        course.setStatus(CourseStatus.DRAFT);
        //When
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(courseRepo.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepo.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));

        Course result = courseService.approveCourseById(courseId, CourseStatus.REJECTED);
        //Then
        verify(courseRepo, times(1)).findById(courseId);
        verify(courseRepo, times(1)).save(course);
        assertEquals(CourseStatus.REJECTED, result.getStatus());
        assertEquals("testuser", result.getUpdatedBy());
        assertNotNull(result.getUpdatedDate());
    }

    @Test
    void testApproveCourseById_InvalidStatus() {
        //Given
        Long courseId = 1L;

        Course result = courseService.approveCourseById(courseId, CourseStatus.DRAFT);
        //Then
        verify(courseRepo, never()).findById(courseId);
        verify(courseRepo, never()).save(any(Course.class));
        assertNull(result);
    }

    @Test
    void testGetCourseByStatus_WithCourses() {
        CourseStatus status = CourseStatus.PUBLISHED;
        int offset = 1;
        int pageSize = 10;
        String sortBy = "name";
        Pageable pageable = PageRequest.of(offset - 1, pageSize);
        Page<Course> coursePage = new PageImpl<>(courses, pageable, courses.size());

        when(pageUtil.getPageable(sortBy, offset - 1, pageSize)).thenReturn(pageable);
        when(courseRepo.findCourseByStatus(status, pageable)).thenReturn(coursePage);

        Page<Course> result = courseService.getCourseByStatus(status, offset, pageSize, sortBy);

        assertEquals(coursePage, result);
        verify(pageUtil, times(1)).checkOffset(offset);
        verify(pageUtil, times(1)).getPageable(sortBy, offset - 1, pageSize);
        verify(courseRepo, times(1)).findCourseByStatus(status, pageable);
    }

    @Test
    void testGetCourseByStatus_WithoutCourses() {
        CourseStatus status = CourseStatus.PUBLISHED;
        int offset = 1;
        int pageSize = 10;
        String sortBy = "name";
        Pageable pageable = PageRequest.of(offset - 1, pageSize);
        Page<Course> emptyCoursePage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(pageUtil.getPageable(sortBy, offset - 1, pageSize)).thenReturn(pageable);
        when(courseRepo.findCourseByStatus(status, pageable)).thenReturn(emptyCoursePage);

        Page<Course> result = courseService.getCourseByStatus(status, offset, pageSize, sortBy);

        assertTrue(result.isEmpty());
        verify(pageUtil, times(1)).checkOffset(offset);
        verify(pageUtil, times(1)).getPageable(sortBy, offset - 1, pageSize);
        verify(courseRepo, times(1)).findCourseByStatus(status, pageable);
    }

    @Test
    void testAddStudiedLesson_ExistingLesson() {
        //Given
        StudiedCourse existingStudiedCourse = new StudiedCourse();
        existingStudiedCourse.setCourseId(1L);
        existingStudiedCourse.setChapterId(1L);
        existingStudiedCourse.setLessonId(1L);
        List<StudiedCourse> studiedCourses = new ArrayList<>();
        studiedCourses.add(existingStudiedCourse);
        //When
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(lessonService.findLessonById(1L)).thenReturn(lesson);

        CustomAccountResDto result = courseService.addStudiedLesson(1L);
        //Then
        assertEquals(1, result.getStudiedCourses().size());
        assertTrue(result.getStudiedCourses().get(0).isCheckPoint());
        verify(accountUtil, times(1)).getCurrentAccount();
        verify(lessonService, times(2)).findLessonById(1L); // Adjust as per actual method calls
    }

    @Test
    void testAddStudiedLesson_NewLesson() {
        //When
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(lessonService.findLessonById(1L)).thenReturn(lesson);

        CustomAccountResDto result = courseService.addStudiedLesson(1L);
        //Then
        assertEquals(1, result.getStudiedCourses().size());
        assertTrue(result.getStudiedCourses().get(0).isCheckPoint());
        assertEquals(1L, result.getStudiedCourses().get(0).getCourseId());
        assertEquals(1L, result.getStudiedCourses().get(0).getChapterId());
        assertEquals(1L, result.getStudiedCourses().get(0).getLessonId());
        verify(accountUtil, times(1)).getCurrentAccount();// Only one invocation expected for new lesson
    }

    @Test
    void testCheckAndUpdateStudiedCourses() {
        // Given
        List<StudiedCourse> studiedCourses = new ArrayList<>();
        StudiedCourse existingStudiedCourse1 = new StudiedCourse();
        existingStudiedCourse1.setCourseId(1L);
        existingStudiedCourse1.setChapterId(1L);
        existingStudiedCourse1.setLessonId(1L);
        studiedCourses.add(existingStudiedCourse1);

        StudiedCourse existingStudiedCourse2 = new StudiedCourse();
        existingStudiedCourse2.setCourseId(2L);
        existingStudiedCourse2.setChapterId(2L);
        existingStudiedCourse2.setLessonId(2L);
        studiedCourses.add(existingStudiedCourse2);

        Long courseId = 1L;
        Long chapterId = 1L;
        Long lessonId = 1L;

        // When
        for (StudiedCourse sc : studiedCourses) {
            if (sc.getCourseId().equals(courseId) && sc.getChapterId().equals(chapterId) && sc.getLessonId().equals(lessonId)) {
                sc.setCheckPoint(true);
            } else {
                sc.setCheckPoint(false);
            }
        }

        // Then
        assertEquals(2, studiedCourses.size()); // Ensure no new items added or removed
        assertTrue(studiedCourses.get(0).isCheckPoint()); // Verify update for existingStudiedCourse1
        assertFalse(studiedCourses.get(1).isCheckPoint()); // Verify update for existingStudiedCourse2
    }

    @Test
    void testAddToWishList_Success() {
        //Given
        Account account = new Account();
        account.setId(1L);
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        List<Long> ids = List.of(1L, 2L, 3L);
        List<Course> courses = new ArrayList<>();
        for (Long id : ids) {
            Course course = new Course();
            course.setId(id);
            courses.add(course);
        }
        //When
        when(courseRepo.findByIdIn(ids)).thenReturn(courses);

        List<Long> existingWishList = List.of(2L, 4L); // Existing wish list IDs

        CustomAccountResDto result = courseService.addToWishList(ids);
        //Then
        assertEquals(account.getId(), result.getId());
        assertTrue(result.getWishListCourses().containsAll(ids)); // Ensure all requested IDs are in the wish list

        verify(accountUtil, times(1)).getCurrentAccount();
        verify(courseRepo, times(1)).findByIdIn(ids);
    }

    @Test
    void testAddToWishList_CourseNotFound() {
        // Given
        Account account = new Account();
        when(accountUtil.getCurrentAccount()).thenReturn(account);

        List<Long> ids = List.of(1L, 2L, 3L);
        List<Course> foundCourses = List.of(new Course(), new Course());
        when(courseRepo.findByIdIn(ids)).thenReturn(foundCourses);

        // When
        assertThrows(AppException.class, () -> courseService.addToWishList(ids));

        // Then
        verify(accountUtil, times(1)).getCurrentAccount(); //Bug : Wanted but not invoked
        verify(courseRepo, times(1)).findByIdIn(ids);
    }

    @Test
    void testGetCourseByIdsIn_Success() {
        // Given
        List<Long> courseIds = List.of(1L, 2L, 3L);
        List<Course> courses = List.of(new Course(), new Course(), new Course());
        when(courseRepo.findByIdIn(courseIds)).thenReturn(courses);

        // When
        List<Course> result = courseService.getCourseByIdsIn(courseIds);

        // Then
        assertEquals(3, result.size());
        verify(courseRepo, times(1)).findByIdIn(courseIds);
    }

    @Test
    void testGetCourseByIdsIn_EmptyList() {
        // Given
        List<Long> courseIds = List.of(1L, 2L, 3L);
        when(courseRepo.findByIdIn(courseIds)).thenReturn(Collections.emptyList());

        // When & Then
        AppException exception = assertThrows(AppException.class, () -> {
            courseService.getCourseByIdsIn(courseIds);
        });

        assertEquals(ErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());
        verify(courseRepo, times(1)).findByIdIn(courseIds);
    }

    @Test
    void testGetAllCourse() {
        // Given
        List<Course> courses = List.of(new Course(), new Course(), new Course());
        when(courseRepo.findAll()).thenReturn(courses);
        Pageable pageable = mock(Pageable.class);
        when(pageUtil.getPageable(anyString(), anyInt(), anyInt())).thenReturn(pageable);

        // When
        Page<Course> result = courseService.getAllCourse(1, 10, "id");

        // Then
        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        verify(courseRepo, times(1)).findAll();
        verify(pageUtil, times(1)).getPageable("id", 0, 10);
    }

    @Test
    void testGetCheckPoint_Found() {
        // Given
        Account account = new Account();
        StudiedCourse studiedCourse1 = new StudiedCourse();
        studiedCourse1.setCheckPoint(false);
        StudiedCourse studiedCourse2 = new StudiedCourse();
        studiedCourse2.setCheckPoint(true);
        List<StudiedCourse> studiedCourses = List.of(studiedCourse1, studiedCourse2);

        when(accountUtil.getCurrentAccount()).thenReturn(account);
        CourseServiceImpl courseServiceSpy = spy(courseService);

        // When
        StudiedCourse result = courseServiceSpy.getCheckPoint();

        // Then

        verify(accountUtil, times(1)).getCurrentAccount();
    }

    @Test
    void testGetCheckPoint_NotFound() {
        // Given
        Account account = new Account();
        StudiedCourse studiedCourse1 = new StudiedCourse();
        studiedCourse1.setCheckPoint(false);
        StudiedCourse studiedCourse2 = new StudiedCourse();
        studiedCourse2.setCheckPoint(false);
        List<StudiedCourse> studiedCourses = List.of(studiedCourse1, studiedCourse2);

        when(accountUtil.getCurrentAccount()).thenReturn(account);
        CourseServiceImpl courseServiceSpy = spy(courseService);

        // When
        StudiedCourse result = courseServiceSpy.getCheckPoint();

        // Then
        verify(accountUtil, times(1)).getCurrentAccount();
    }
    //chuwua xong
    @Test
    void testGetStudiedCourses_Success() throws Exception {
        // Given

        // When

        // Then

    }
    //chuwua xong
    @Test
    void testGetStudiedCourses_JsonProcessingException() throws Exception {
        // Given


        // When


        // Then

    }

    @Test
    void testSaveCourse() {
        // Given
        Course course = new Course();
        course.setId(1L);
        course.setName("Test Course");

        // When
        courseService.saveCourse(course);

        // Then
        verify(courseRepo, times(1)).save(course);
    }

    @Test
    void testGetGeneralCourseByName_Success() {
        // Given
        String name = "Java";
        int offset = 1;
        int pageSize = 10;
        String sortBy = "name";
        Course course1 = new Course();
        course1.setId(1L);
        Course course2 = new Course();
        course2.setId(2L);

        List<Course> foundCourses = List.of(course1, course2);
        List<Long> courseIds = List.of(1L, 2L);
        Pageable pageable = PageRequest.of(offset - 1, pageSize, Sort.by(sortBy));
        Page<Course> coursePage = new PageImpl<>(foundCourses, pageable, foundCourses.size());
        doReturn(foundCourses).when(courseRepo).findCourseByNameLike("%" + name + "%");
        doReturn(pageable).when(pageUtil).getPageable(sortBy, offset - 1, pageSize);
        doReturn(coursePage).when(courseRepo).findByIdInAndStatus(courseIds, CourseStatus.PUBLISHED, pageable);

        // When
        Page<GeneralCourse> result = courseService.getGeneralCourseByName(name, offset, pageSize, sortBy);

        // Then
        assertNotNull(result);
        assertEquals(foundCourses.size(), result.getTotalElements());
        verify(pageUtil, times(1)).checkOffset(offset);
        verify(pageUtil, times(1)).getPageable(sortBy, offset - 1, pageSize);
        verify(courseRepo, times(1)).findCourseByNameLike("%" + name + "%");
        verify(courseRepo, times(1)).findByIdInAndStatus(courseIds, CourseStatus.PUBLISHED, pageable);
    }

    @Test
    void testGetGeneralCourseByName_NoCoursesFound() {
        // Given
        String name = "NonExistent";
        int offset = 1;
        int pageSize = 10;
        String sortBy = "name";

        List<Course> foundCourses = List.of();
        List<Long> courseIds = List.of();
        Pageable pageable = PageRequest.of(offset - 1, pageSize, Sort.by(sortBy));
        Page<Course> coursePage = new PageImpl<>(foundCourses, pageable, foundCourses.size());

        // When
        when(courseRepo.findCourseByNameLike("%" + name + "%")).thenReturn(foundCourses);
        when(pageUtil.getPageable(sortBy, offset - 1, pageSize)).thenReturn(pageable);
        when(courseRepo.findByIdInAndStatus(courseIds, CourseStatus.PUBLISHED, pageable)).thenReturn(coursePage);

        Page<GeneralCourse> result = courseService.getGeneralCourseByName(name, offset, pageSize, sortBy);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(pageUtil, times(1)).checkOffset(offset);
        verify(pageUtil, times(1)).getPageable(sortBy, offset - 1, pageSize);
        verify(courseRepo, times(1)).findCourseByNameLike("%" + name + "%");
        verify(courseRepo, times(1)).findByIdInAndStatus(courseIds, CourseStatus.PUBLISHED, pageable);
    }

//    @Test
//    void testSaveWishList() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
//        setMethod(courseServiceImpl, "saveWishListCourses");
//    }
//
//    private void setMethod(Object targetObject, String fieldName, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//        Method method = targetObject.getClass().getDeclaredMethod(fieldName);
//        method.setAccessible(true);
//        method.invoke(targetObject, args);
//    }
}
