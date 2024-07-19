package com.fpt.cursus.service;


import com.fpt.cursus.dto.request.CreateCourseDto;
import com.fpt.cursus.dto.request.UpdateCourseDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
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

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    private LessonService lessonService;

    @Mock
    private Account account;

    @Mock
    private AccountUtil accountUtil;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Course sampleCourse;
    private CreateCourseDto sampleCreateCourseDto;
    private UpdateCourseDto sampleUpdateCourseDto;
    private Account mockAccount;


    @BeforeEach
    void setUp() {
        account = new Account();
        account.setUsername("testuser");


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


}
