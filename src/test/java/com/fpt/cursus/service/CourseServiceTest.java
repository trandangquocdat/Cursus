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
import com.fpt.cursus.service.impl.CourseServiceImpl;
import com.fpt.cursus.util.AccountUtil;
import com.fpt.cursus.util.FileUtil;
import com.fpt.cursus.util.PageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

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
    private LessonService lessonService;

    @Mock
    private AccountUtil accountUtil;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private CourseServiceImpl courseService;

    private List<Course> courses;
    private Course sampleCourse;
    private UpdateCourseDto sampleUpdateCourseDto;
    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setUsername("testUser");
        courses = List.of(new Course(), new Course());

        sampleCourse = new Course();
        sampleCourse.setId(1L);
        sampleCourse.setName("Sample Course");
        sampleCourse.setPrice(500000);
        sampleCourse.setStatus(CourseStatus.DRAFT);
        sampleCourse.setStatus(CourseStatus.ACTIVE);

        sampleUpdateCourseDto = new UpdateCourseDto();
        sampleUpdateCourseDto.setName("Updated Course");
        sampleUpdateCourseDto.setPrice(300000.0);
        sampleUpdateCourseDto.setPictureLink(null);
    }

    @Test
    void testCreateCourse_InvalidImage() {
        //given
        MultipartFile image = new MockMultipartFile("image",
                "test.jpg", "image/jpeg", new byte[0]);

        CreateCourseDto createCourseDto = new CreateCourseDto();
        createCourseDto.setName("New Course");
        createCourseDto.setPrice(200000.0);
        createCourseDto.setPictureLink(image);
        createCourseDto.setCategory(Category.ALL);
        createCourseDto.setDescription("Description");
        //when
        when(modelMapper.map(any(CreateCourseDto.class), any())).thenReturn(sampleCourse);
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(fileUtil.isImage(any(MultipartFile.class))).thenReturn(false);
        //then
        assertThrows(AppException.class,
                () -> courseService.createCourse(createCourseDto),
                ErrorCode.FILE_INVALID_IMAGE.getMessage());
    }

    @Test
    void testCreateCourse_Success() {
        //Given
        MultipartFile image = new MockMultipartFile("image",
                "test.jpg", "image/jpeg", new byte[0]);

        CreateCourseDto createCourseDto = new CreateCourseDto();
        createCourseDto.setName("New Course");
        createCourseDto.setPrice(200000.0);
        createCourseDto.setPictureLink(image);
        createCourseDto.setCategory(Category.ALL);
        createCourseDto.setDescription("Description");

        //When
        when(modelMapper.map(any(CreateCourseDto.class), any())).thenReturn(sampleCourse);
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(fileUtil.isImage(any(MultipartFile.class))).thenReturn(true);
        when(fileService.linkSave(any(MultipartFile.class), anyString())).thenReturn("test.jpg");
        when(courseRepo.save(any(Course.class))).thenReturn(sampleCourse);

        Course result = courseService.createCourse(createCourseDto);

        //Then
        assertEquals(sampleCourse, result);
        assertEquals("testUser", result.getCreatedBy());
        assertEquals(CourseStatus.DRAFT, result.getStatus());
        assertEquals("test.jpg", result.getPictureLink());
        assertNotNull(result.getCreatedDate());
    }

    @Test
    void testCreateCoursePictureLinkNull() {
        //Given
        CreateCourseDto createCourseDto = new CreateCourseDto();
        createCourseDto.setName("New Course");
        createCourseDto.setPrice(200000.0);
        createCourseDto.setPictureLink(null);
        createCourseDto.setCategory(Category.ALL);
        createCourseDto.setDescription("Description");

        //When
        when(modelMapper.map(any(CreateCourseDto.class), any())).thenReturn(sampleCourse);
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(courseRepo.save(any(Course.class))).thenReturn(sampleCourse);

        Course result = courseService.createCourse(createCourseDto);

        //Then
        assertEquals(sampleCourse, result);
        assertEquals("testUser", result.getCreatedBy());
        assertEquals(CourseStatus.DRAFT, result.getStatus());
        assertEquals("defaultImage.jpg", result.getPictureLink());
        assertNotNull(result.getCreatedDate());
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
        assertEquals("testUser", deletedCourse.getUpdatedBy());
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
        //given
        sampleCourse.setStatus(CourseStatus.DELETED);
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(courseRepo.findById(1L)).thenReturn(Optional.of(sampleCourse));
        //Then
        assertThrows(AppException.class,
                () -> courseService.updateCourse(1L, sampleUpdateCourseDto),
                ErrorCode.COURSE_NOT_FOUND.getMessage());
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
        when(fileService.linkSave(any(MultipartFile.class), anyString())).thenReturn("test.jpg");
        when(courseRepo.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateCourseDto updateDto = new UpdateCourseDto();
        updateDto.setName("Updated Course");
        updateDto.setPrice(60000.0);
        updateDto.setPictureLink(new MockMultipartFile("image",
                "test.jpg", "image/jpeg", new byte[0]));

        Course updatedCourse = courseService.updateCourse(courseId, updateDto);

        //Then
        assertEquals(updateDto.getName(), updatedCourse.getName());
        assertEquals(updateDto.getPrice(), updatedCourse.getPrice());
        assertEquals("test.jpg", updatedCourse.getPictureLink());
        assertEquals(CourseStatus.DRAFT, updatedCourse.getStatus());
        assertEquals("testUser", updatedCourse.getUpdatedBy());

        assertNotNull(updatedCourse.getUpdatedDate());

        verify(courseRepo, times(1)).findById(courseId);
        verify(courseRepo, times(1)).save(existingCourse);
    }

    @Test
    void testUpdateCourse_CourseExists() {
        //Given
        Long courseId = 1L;
        Course existingCourse = new Course();
        existingCourse.setId(courseId);
        existingCourse.setName("Existing Course");
        existingCourse.setStatus(CourseStatus.DRAFT);

        //When
        when(accountUtil.getCurrentAccount()).thenReturn(account);
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
    void testUpdateCourse_InvalidPriceTooLow() {
        //Given
        Long courseId = 1L;
        Course existingCourse = new Course();
        existingCourse.setId(courseId);
        existingCourse.setName("Existing Course");
        existingCourse.setStatus(CourseStatus.DRAFT);

        //When
        when(accountUtil.getCurrentAccount()).thenReturn(account);
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
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(courseRepo.findById(courseId)).thenReturn(Optional.of(existingCourse));
        when(courseRepo.existsByName("Existing Course")).thenReturn(true);

        UpdateCourseDto updateDto = new UpdateCourseDto();
        updateDto.setName("Existing Course");
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
    void testUpdateCourse_PictureLinkNull() {
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
        List<Course> courseList = List.of(course1, course2);
        Page<Course> coursePage = new PageImpl<>(courseList);

        when(courseRepo.findCourseByCreatedBy(anyString(), any(Pageable.class))).thenReturn(coursePage);

        Page<Course> result = courseService.getCourseByCreatedBy(offset, pageSize, sortBy);

        assertFalse(result.isEmpty());
        assertEquals(2, result.getTotalElements());
        assertEquals("Course 1", result.getContent().get(0).getName());
        assertEquals("Course 2", result.getContent().get(1).getName());
    }

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
        assertEquals("testUser", result.getUpdatedBy());
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
        assertEquals("testUser", result.getUpdatedBy());
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
        Pageable pageable = PageRequest.of(0, pageSize);
        Page<Course> coursePage = new PageImpl<>(courses, pageable, courses.size());

        when(pageUtil.getPageable(sortBy, 0, pageSize)).thenReturn(pageable);
        when(courseRepo.findCourseByStatus(status, pageable)).thenReturn(coursePage);

        Page<Course> result = courseService.getCourseByStatus(status, offset, pageSize, sortBy);

        assertEquals(coursePage, result);
        verify(pageUtil, times(1)).checkOffset(offset);
        verify(pageUtil, times(1)).getPageable(sortBy, 0, pageSize);
        verify(courseRepo, times(1)).findCourseByStatus(status, pageable);
    }

    @Test
    void testGetCourseByStatus_WithoutCourses() {
        CourseStatus status = CourseStatus.PUBLISHED;
        int offset = 1;
        int pageSize = 10;
        String sortBy = "name";
        Pageable pageable = PageRequest.of(0, pageSize);
        Page<Course> emptyCoursePage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(pageUtil.getPageable(sortBy, 0, pageSize)).thenReturn(pageable);
        when(courseRepo.findCourseByStatus(status, pageable)).thenReturn(emptyCoursePage);

        Page<Course> result = courseService.getCourseByStatus(status, offset, pageSize, sortBy);

        assertTrue(result.isEmpty());
        verify(pageUtil, times(1)).checkOffset(offset);
        verify(pageUtil, times(1)).getPageable(sortBy, 0, pageSize);
        verify(courseRepo, times(1)).findCourseByStatus(status, pageable);
    }

    @Test
    void testGetCourseByCreateByUsername_WithCourses() {
        String username = "testUser";
        when(courseRepo.findCourseByCreatedBy(username)).thenReturn(courses);

        List<Course> result = courseService.getCourseByCreatedBy(username);

        assertEquals(courses, result);
        verify(courseRepo, times(1)).findCourseByCreatedBy(username);
    }

    @Test
    void testGetCourseByCreateByUsername_WithoutCourses() {
        String username = "testUser";
        when(courseRepo.findCourseByCreatedBy(username)).thenReturn(Collections.emptyList());

        List<Course> result = courseService.getCourseByCreatedBy(username);

        assertTrue(result.isEmpty());
        verify(courseRepo, times(1)).findCourseByCreatedBy(username);
    }

    @Test
    void testAddToWishList_Success() {
        //Given
        account.setId(1L);
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        List<Long> ids = List.of(1L, 2L, 3L);
        List<Course> list = new ArrayList<>();
        for (Long id : ids) {
            Course course = new Course();
            course.setId(id);
            list.add(course);
        }
        //When
        when(courseRepo.findByIdIn(ids)).thenReturn(list);

        // Existing wish list IDs

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
        List<Long> ids = List.of(1L, 2L, 3L);
        List<Course> foundCourses = List.of(new Course(), new Course());

        // When
        when(courseRepo.findByIdIn(ids)).thenReturn(foundCourses);

        // Then
        assertThrows(AppException.class,
                () -> courseService.addToWishList(ids),
                ErrorCode.COURSE_NOT_FOUND.getMessage());
        verify(courseRepo, times(1)).findByIdIn(ids);
    }

    @Test
    void testGetCourseByIdsIn_Success() {
        // Given
        List<Long> courseIds = List.of(1L, 2L, 3L);
        List<Course> list = List.of(new Course(), new Course(), new Course());
        when(courseRepo.findByIdIn(courseIds)).thenReturn(list);

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
        List<Course> list = List.of(new Course(), new Course(), new Course());
        when(courseRepo.findAll()).thenReturn(list);
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
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(sortBy));
        Page<Course> coursePage = new PageImpl<>(foundCourses, pageable, foundCourses.size());
        doReturn(foundCourses).when(courseRepo).findCourseByNameLike("%" + name + "%");
        doReturn(pageable).when(pageUtil).getPageable(sortBy, 0, pageSize);
        doReturn(coursePage).when(courseRepo).findByIdInAndStatus(courseIds, CourseStatus.PUBLISHED, pageable);

        // When
        Page<GeneralCourse> result = courseService.getGeneralCourseByName(name, offset, pageSize, sortBy);

        // Then
        assertNotNull(result);
        assertEquals(foundCourses.size(), result.getTotalElements());
        verify(pageUtil, times(1)).checkOffset(offset);
        verify(pageUtil, times(1)).getPageable(sortBy, 0, pageSize);
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
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(sortBy));
        Page<Course> coursePage = new PageImpl<>(foundCourses, pageable, 0);

        // When
        when(courseRepo.findCourseByNameLike("%" + name + "%")).thenReturn(foundCourses);
        when(pageUtil.getPageable(sortBy, 0, pageSize)).thenReturn(pageable);
        when(courseRepo.findByIdInAndStatus(courseIds, CourseStatus.PUBLISHED, pageable)).thenReturn(coursePage);

        Page<GeneralCourse> result = courseService.getGeneralCourseByName(name, offset, pageSize, sortBy);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(pageUtil, times(1)).checkOffset(offset);
        verify(pageUtil, times(1)).getPageable(sortBy, 0, pageSize);
        verify(courseRepo, times(1)).findCourseByNameLike("%" + name + "%");
        verify(courseRepo, times(1)).findByIdInAndStatus(courseIds, CourseStatus.PUBLISHED, pageable);
    }

    @Test
    void testAddStudiedLessonHaveStudiedCoursesAndExists() throws JsonProcessingException {
        //given
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        Chapter chapter = new Chapter();
        chapter.setId(1L);
        Course course = new Course();
        course.setId(1L);
        chapter.setCourse(course);
        lesson.setChapter(chapter);

        account.setStudiedCourseJson("[]");

        List<StudiedCourse> studiedCourses = new ArrayList<>();
        StudiedCourse studiedCourse = new StudiedCourse();
        studiedCourse.setCourseId(1L);
        studiedCourse.setChapterId(1L);
        studiedCourse.setLessonId(1L);
        studiedCourses.add(studiedCourse);
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(lessonService.findLessonById(anyLong())).thenReturn(lesson);
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(studiedCourses);
        //then
        CustomAccountResDto result = courseService.addStudiedLesson(1L);
        assertEquals(account.getId(), result.getId());
        assertEquals(1, result.getStudiedCourses().size());
        assertEquals(1L, result.getStudiedCourses().get(0).getCourseId());
    }

    @Test
    void testAddStudiedLessonHaveStudiedCoursesAndNotExistsLesson() throws JsonProcessingException {
        //given
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        Chapter chapter = new Chapter();
        chapter.setId(1L);
        Course course = new Course();
        course.setId(1L);
        chapter.setCourse(course);
        lesson.setChapter(chapter);

        account.setStudiedCourseJson("[]");

        List<StudiedCourse> studiedCourses = new ArrayList<>();
        StudiedCourse studiedCourse = new StudiedCourse();
        studiedCourse.setCourseId(1L);
        studiedCourse.setChapterId(1L);
        studiedCourse.setLessonId(2L);
        studiedCourses.add(studiedCourse);
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(lessonService.findLessonById(anyLong())).thenReturn(lesson);
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(studiedCourses);
        //then
        CustomAccountResDto result = courseService.addStudiedLesson(1L);
        assertEquals(account.getId(), result.getId());
        assertEquals(2, result.getStudiedCourses().size());
        assertEquals(1L, result.getStudiedCourses().get(0).getCourseId());
    }

    @Test
    void testAddStudiedLessonHaveStudiedCoursesAndNotExistsCourse() throws JsonProcessingException {
        //given
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        Chapter chapter = new Chapter();
        chapter.setId(1L);
        Course course = new Course();
        course.setId(1L);
        chapter.setCourse(course);
        lesson.setChapter(chapter);

        account.setStudiedCourseJson("[]");

        List<StudiedCourse> studiedCourses = new ArrayList<>();
        StudiedCourse studiedCourse = new StudiedCourse();
        studiedCourse.setCourseId(2L);
        studiedCourse.setChapterId(1L);
        studiedCourse.setLessonId(1L);
        studiedCourses.add(studiedCourse);
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(lessonService.findLessonById(anyLong())).thenReturn(lesson);
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(studiedCourses);
        //then
        CustomAccountResDto result = courseService.addStudiedLesson(1L);
        assertEquals(account.getId(), result.getId());
        assertEquals(2, result.getStudiedCourses().size());
        assertEquals(2L, result.getStudiedCourses().get(0).getCourseId());
        assertEquals(1L, result.getStudiedCourses().get(0).getChapterId());
    }

    @Test
    void testAddStudiedLessonHaveStudiedCoursesAndNotExistsChapter() throws JsonProcessingException {
        //given
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        Chapter chapter = new Chapter();
        chapter.setId(1L);
        Course course = new Course();
        course.setId(1L);
        chapter.setCourse(course);
        lesson.setChapter(chapter);

        account.setStudiedCourseJson("[]");

        List<StudiedCourse> studiedCourses = new ArrayList<>();
        StudiedCourse studiedCourse = new StudiedCourse();
        studiedCourse.setCourseId(1L);
        studiedCourse.setChapterId(2L);
        studiedCourse.setLessonId(2L);
        studiedCourses.add(studiedCourse);
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(lessonService.findLessonById(anyLong())).thenReturn(lesson);
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(studiedCourses);
        //then
        CustomAccountResDto result = courseService.addStudiedLesson(1L);
        assertEquals(account.getId(), result.getId());
        assertEquals(2, result.getStudiedCourses().size());
        assertEquals(1L, result.getStudiedCourses().get(0).getCourseId());
        assertEquals(2L, result.getStudiedCourses().get(0).getChapterId());
    }

    @Test
    void testRemoveFromWishListEmptyWishList() {
        //given
        account.setWishListCourseJson("");
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        //then
        CustomAccountResDto result = courseService.removeFromWishList(1L);
        assertEquals(account.getId(), result.getId());
        assertTrue(result.getWishListCourses().isEmpty());
    }

    @Test
    void testGetWishListCoursesFail() throws JsonProcessingException {
        //given
        account.setWishListCourseJson("[]");
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenThrow(JsonProcessingException.class);
        //then
        assertThrows(AppException.class,
                () -> courseService.getWishListCourses(1, 10, "id"),
                ErrorCode.PROCESS_ADD_STUDIED_COURSE_FAIL.getMessage());
    }

    @Test
    void testGetWishListCoursesSuccess() throws JsonProcessingException {
        //given
        account.setWishListCourseJson("[]");
        int offset = 1;
        int pageSize = 10;
        String sortBy = "name";
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(sortBy));
        sampleCourse.setId(1L);
        List<Course> list = List.of(sampleCourse);
        Page<Course> coursePage = new PageImpl<>(list, pageable, list.size());
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(new ArrayList<>());
        when(pageUtil.getPageable(sortBy, 0, pageSize)).thenReturn(pageable);
        when(courseRepo.findByIdInAndStatus(anyList(), any(CourseStatus.class), any(Pageable.class)))
                .thenReturn(coursePage);
        //then
        Page<GeneralCourse> result = courseService.getWishListCourses(offset, pageSize, sortBy);
        assertEquals(coursePage.getContent().get(0).getId(),
                result.getContent().get(0).getId());
    }

    @Test
    void testGetCourseByCategoryAll() {
        //given
        int offset = 1;
        int pageSize = 10;
        String sortBy = "name";
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(sortBy));
        Page<Course> coursePage = new PageImpl<>(courses, pageable, courses.size());
        //when
        when(pageUtil.getPageable(sortBy, 0, pageSize)).thenReturn(pageable);
        when(courseRepo.findAllByStatus(any(CourseStatus.class), any(Pageable.class)))
                .thenReturn(coursePage);
        //then
        Page<GeneralCourse> result = courseService.getCourseByCategory(Category.ALL, offset, pageSize, sortBy);
        assertEquals(coursePage.getContent().size(), result.getContent().size());
    }

    @Test
    void testGetCourseByCategoryEmptyCourses() {
        //given
        int offset = 1;
        int pageSize = 10;
        String sortBy = "name";
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(sortBy));
        Page<Course> coursePage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        //when
        when(pageUtil.getPageable(sortBy, 0, pageSize)).thenReturn(pageable);
        when(courseRepo.findCourseByCategoryAndStatus(any(Category.class), any(CourseStatus.class), any(Pageable.class)))
                .thenReturn(coursePage);
        //then
        Page<GeneralCourse> result = courseService.getCourseByCategory(Category.FINANCE, offset, pageSize, sortBy);
        assertTrue(result.isEmpty());
    }

    @Test
    void testPercentDoneCourseTotalLessons0() {
        //given
        Course course = new Course();
        Chapter chapter = new Chapter();
        chapter.setLesson(List.of());
        course.setChapter(List.of(chapter));
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(courseRepo.findById(anyLong())).thenReturn(Optional.of(course));
        //then
        double result = courseService.percentDoneCourse(1L);
        assertEquals(0, result);
    }

    @Test
    void testPercentDoneCourseTotalLessons1() {
        //given
        Course course = new Course();
        Chapter chapter = new Chapter();
        Lesson lesson = new Lesson();
        chapter.setLesson(List.of(lesson));
        course.setChapter(List.of(chapter));
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(courseRepo.findById(anyLong())).thenReturn(Optional.of(course));
        //then
        double result = courseService.percentDoneCourse(1L);
        assertEquals(0, result);
    }

    @Test
    void testGetAllGeneralCourses() {
        //given
        int offset = 1;
        int pageSize = 10;
        String sortBy = "name";
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(sortBy));
        List<Course> list = new ArrayList<>();
        list.add(sampleCourse);
        Page<Course> coursePage = new PageImpl<>(list, pageable, list.size());
        //when
        when(pageUtil.getPageable(anyString(), anyInt(), anyInt())).thenReturn(pageable);
        when(courseRepo.findCourseByStatus(any(CourseStatus.class), any(Pageable.class)))
                .thenReturn(coursePage);
        //then
        Page<GeneralCourse> result = courseService.getAllGeneralCourses(sortBy, pageSize, offset);
        assertEquals(coursePage.getContent().size(), result.getContent().size());
    }

    @Test
    void testGetGeneralEnrolledCourses() throws JsonProcessingException {
        //given
        int offset = 1;
        int pageSize = 10;
        String sortBy = "name";
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(sortBy));
        List<Course> list = new ArrayList<>();
        list.add(sampleCourse);
        Page<Course> coursePage = new PageImpl<>(list, pageable, list.size());

        account.setEnrolledCourseJson("[1]");
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(pageUtil.getPageable(anyString(), anyInt(), anyInt())).thenReturn(pageable);
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(List.of(1L));
        when(courseRepo.findByIdInAndStatus(anyList(), any(CourseStatus.class), any(Pageable.class)))
                .thenReturn(coursePage);
        //then
        Page<GeneralCourse> result = courseService.getGeneralEnrolledCourses(sortBy, pageSize, offset);
        assertEquals(coursePage.getContent().size(), result.getContent().size());
    }

    @Test
    void testGetGeneralEnrolledCoursesNullEnrolled() {
        //given
        int offset = 1;
        int pageSize = 10;
        String sortBy = "name";
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(sortBy));
        Page<Course> coursePage = new PageImpl<>(List.of(), pageable, 0);

        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(pageUtil.getPageable(anyString(), anyInt(), anyInt())).thenReturn(pageable);
        //then
        Page<GeneralCourse> result = courseService.getGeneralEnrolledCourses(sortBy, pageSize, offset);
        assertEquals(coursePage.getContent().size(), result.getContent().size());
    }

    @Test
    void testGetGeneralEnrolledCoursesEmptyEnrolled() {
        //given
        int offset = 1;
        int pageSize = 10;
        String sortBy = "name";
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(sortBy));
        Page<Course> coursePage = new PageImpl<>(List.of(), pageable, 0);

        account.setEnrolledCourseJson("");
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(pageUtil.getPageable(anyString(), anyInt(), anyInt())).thenReturn(pageable);
        //then
        Page<GeneralCourse> result = courseService.getGeneralEnrolledCourses(sortBy, pageSize, offset);
        assertEquals(coursePage.getContent().size(), result.getContent().size());
    }

    @Test
    void testGetGeneralEnrolledCoursesCourseNotFound() throws JsonProcessingException {
        //given
        int offset = 1;
        int pageSize = 10;
        String sortBy = "name";
        account.setEnrolledCourseJson("invalid");
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenThrow(JsonProcessingException.class);
        //then
        assertThrows(AppException.class,
                () -> courseService.getGeneralEnrolledCourses(sortBy, pageSize, offset),
                ErrorCode.COURSE_NOT_FOUND.getMessage());
    }

    @Test
    void testGetDetailEnrolledCourses() throws JsonProcessingException {
        //given
        int offset = 1;
        int pageSize = 10;
        String sortBy = "name";
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(sortBy));
        List<Course> list = new ArrayList<>();
        list.add(sampleCourse);
        Page<Course> coursePage = new PageImpl<>(list, pageable, list.size());

        account.setEnrolledCourseJson("[1]");
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(pageUtil.getPageable(anyString(), anyInt(), anyInt())).thenReturn(pageable);
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(List.of(1L));
        when(courseRepo.findByIdInAndStatus(anyList(), any(CourseStatus.class), any(Pageable.class)))
                .thenReturn(coursePage);
        //then
        Page<Course> result = courseService.getDetailEnrolledCourses(sortBy, pageSize, offset);
        assertEquals(coursePage.getContent().size(), result.getContent().size());
    }

    @Test
    void testGetAllStudiedCoursesEmptyStudied() {
        //given
        account.setStudiedCourseJson("");
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        //then
        List<StudiedCourse> result = courseService.getAllStudiedCourses();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCheckPointNull() {
        //given
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        //then
        StudiedCourse result = courseService.getCheckPoint();
        assertNull(result);
    }

    @Test
    void testGetCheckPointNotNull() throws JsonProcessingException {
        //given
        account.setStudiedCourseJson("[1]");
        StudiedCourse studiedCourse = new StudiedCourse();
        studiedCourse.setCourseId(1L);
        studiedCourse.setChapterId(1L);
        studiedCourse.setLessonId(1L);
        studiedCourse.setCheckPoint(true);
        List<StudiedCourse> studiedCourses = List.of(studiedCourse);
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(studiedCourses);
        //then
        StudiedCourse result = courseService.getCheckPoint();
        assertEquals(studiedCourse.getCourseId(), result.getCourseId());
    }

    @Test
    void testGetCheckPointNotNullAndCheckPointFalse() throws JsonProcessingException {
        //given
        account.setStudiedCourseJson("[1]");
        StudiedCourse studiedCourse = new StudiedCourse();
        studiedCourse.setCourseId(1L);
        studiedCourse.setChapterId(1L);
        studiedCourse.setLessonId(1L);
        studiedCourse.setCheckPoint(false);
        List<StudiedCourse> studiedCourses = List.of(studiedCourse);
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(studiedCourses);
        //then
        StudiedCourse result = courseService.getCheckPoint();
        assertNull(result);
    }

    @Test
    void testGetCheckPointFail() throws JsonProcessingException {
        //given
        account.setStudiedCourseJson("invalid");
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenThrow(JsonProcessingException.class);
        //then
        assertThrows(AppException.class,
                () -> courseService.getCheckPoint(),
                ErrorCode.PROCESS_ADD_STUDIED_COURSE_FAIL.getMessage());
    }

    @Test
    void testCreateCourseCourseExists() {
        //given
        CreateCourseDto createCourseDto = new CreateCourseDto();
        createCourseDto.setName("Java");
        //when
        when(courseRepo.existsByName(anyString())).thenReturn(true);
        //then
        assertThrows(AppException.class,
                () -> courseService.createCourse(createCourseDto),
                ErrorCode.COURSE_EXISTS.getMessage());
    }

    @Test
    void testCreateCourseCoursePriceTooLow() {
        //given
        CreateCourseDto createCourseDto = new CreateCourseDto();
        createCourseDto.setName("Java");
        createCourseDto.setPrice(0.0);
        //when
        when(courseRepo.existsByName(anyString())).thenReturn(false);
        //then
        assertThrows(AppException.class,
                () -> courseService.createCourse(createCourseDto),
                ErrorCode.COURSE_PRICE_INVALID.getMessage());
    }

    @Test
    void testCreateCourseCoursePriceTooHigh() {
        //given
        CreateCourseDto createCourseDto = new CreateCourseDto();
        createCourseDto.setName("Java");
        createCourseDto.setPrice(100000000.0);
        //when
        when(courseRepo.existsByName(anyString())).thenReturn(false);
        //then
        assertThrows(AppException.class,
                () -> courseService.createCourse(createCourseDto),
                ErrorCode.COURSE_PRICE_INVALID.getMessage());
    }

    @Test
    void testAddStudiedLessonFail() throws JsonProcessingException {
        //given
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        Chapter chapter = new Chapter();
        chapter.setId(1L);
        Course course = new Course();
        course.setId(1L);
        chapter.setCourse(course);
        lesson.setChapter(chapter);

        account.setStudiedCourseJson("[]");

        List<StudiedCourse> studiedCourses = new ArrayList<>();
        StudiedCourse studiedCourse = new StudiedCourse();
        studiedCourse.setCourseId(1L);
        studiedCourse.setChapterId(2L);
        studiedCourse.setLessonId(2L);
        studiedCourses.add(studiedCourse);
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(lessonService.findLessonById(anyLong())).thenReturn(lesson);
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(studiedCourses);
        when(objectMapper.writeValueAsString(anyList())).thenThrow(JsonProcessingException.class);
        //then
        assertThrows(AppException.class,
                () -> courseService.addStudiedLesson(1L),
                ErrorCode.PROCESS_ADD_STUDIED_COURSE_FAIL.getMessage());
    }

    @Test
    void testRemoveFromWishListFail() throws JsonProcessingException {
        //given
        account.setWishListCourseJson("");
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(objectMapper.writeValueAsString(anyList())).thenThrow(JsonProcessingException.class);
        //then
        assertThrows(AppException.class,
                () -> courseService.removeFromWishList(1L),
                ErrorCode.PROCESS_ADD_STUDIED_COURSE_FAIL.getMessage());
    }

}
