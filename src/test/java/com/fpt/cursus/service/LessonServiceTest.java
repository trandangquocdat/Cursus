package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.CreateLessonDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Chapter;
import com.fpt.cursus.entity.Lesson;
import com.fpt.cursus.enums.status.LessonStatus;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.LessonRepo;
import com.fpt.cursus.util.AccountUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @InjectMocks
    private LessonService lessonService;

    @Mock
    private LessonRepo lessonRepo;

    @Mock
    private ChapterService chapterService;

    @Mock
    private AccountUtil accountUtil;

    private Account account;
    private Chapter chapter;
    private Date date;
    private Long id;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setUsername("admin");

        chapter = new Chapter();
        chapter.setId(1L);

        date = new Date();
        id = 1L;
    }

    @Test
    void testCreateLessonSuccessfully() {
        CreateLessonDto request = new CreateLessonDto();
        request.setName("New Lesson");
        request.setDescription("New Description");

        Lesson lesson = new Lesson();
        lesson.setName(request.getName());
        lesson.setDescription(request.getDescription());
        lesson.setChapter(chapter);
        lesson.setCreatedDate(date);
        lesson.setCreatedBy(account.getUsername());

        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(chapterService.findChapterById(1L)).thenReturn(chapter);
        when(lessonRepo.save(any(Lesson.class))).thenReturn(lesson);

        Lesson result = lessonService.createLesson(id, request);

        assertNotNull(result);
        assertEquals(request.getName(), result.getName());
        assertEquals(request.getDescription(), result.getDescription());
        assertEquals(chapter, result.getChapter());
        assertEquals(account.getUsername(), result.getCreatedBy());
        assertEquals(date, result.getCreatedDate());
        verify(lessonRepo, times(1)).save(any(Lesson.class));
    }

    @Test
    void testCreateLessonNotFoundChapter() {
        CreateLessonDto request = new CreateLessonDto();
        request.setName("New Lesson");
        request.setDescription("New Description");

        when(chapterService.findChapterById(1L)).thenReturn(null);

        assertThrows(AppException.class,
                () -> lessonService.createLesson(id, request),
                ErrorCode.CHAPTER_NOT_FOUND.getMessage());
    }

    @Test
    void testFindLessonByIdSuccessfully() {
        Lesson lesson = new Lesson();
        lesson.setId(id);
        when(lessonRepo.findLessonById(anyLong())).thenReturn(lesson);

        Lesson result = lessonService.findLessonById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void testFindLessonByIdFailed() {
        when(lessonRepo.findLessonById(anyLong())).thenReturn(null);

        assertNull(lessonService.findLessonById(id));
    }

    @Test
    void testDeleteLessonByIdSuccessfully() {
        Lesson lesson = new Lesson();
        lesson.setStatus(LessonStatus.ACTIVE);
        lesson.setChapter(chapter);
        when(lessonRepo.findLessonById(anyLong())).thenReturn(lesson);

        lessonService.deleteLessonById(id);

        assertNull(lesson.getChapter());
        assertEquals(LessonStatus.DELETED, lesson.getStatus());
        verify(lessonRepo, times(1)).save(any(Lesson.class));
    }

    @Test
    void testDeleteLessonByIdNotFoundLessonId() {
        when(lessonRepo.findLessonById(anyLong())).thenReturn(null);

        assertThrows(AppException.class,
                () -> lessonService.deleteLessonById(id),
                ErrorCode.LESSON_NOT_FOUND.getMessage());
    }

    @Test
    void testUpdateLessonSuccessfully() {
        CreateLessonDto request = new CreateLessonDto();
        request.setName("New Lesson");
        request.setDescription("New Description");

        Lesson lesson = new Lesson();
        lesson.setId(id);
        lesson.setName("Old Lesson");
        lesson.setDescription("Old Description");
        lesson.setUpdatedBy(account.getUsername());
        lesson.setUpdatedDate(date);
        when(lessonRepo.findLessonById(anyLong())).thenReturn(lesson);
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(lessonRepo.save(any(Lesson.class))).thenReturn(lesson);

        lessonService.updateLesson(id, request);

        assertNotNull(lesson);
        assertEquals(id, lesson.getId());
        assertEquals(request.getName(), lesson.getName());
        assertEquals(request.getDescription(), lesson.getDescription());
        assertNotNull(lesson.getUpdatedDate());
        assertEquals(account.getUsername(), lesson.getUpdatedBy());
        verify(lessonRepo, times(1)).save(any(Lesson.class));
    }

    @Test
    void testUpdateLessonNotFoundLessonId() {
        CreateLessonDto request = new CreateLessonDto();
        request.setName("New Lesson");
        request.setDescription("New Description");

        when(lessonRepo.findLessonById(anyLong())).thenReturn(null);

        assertThrows(AppException.class,
                () -> lessonService.updateLesson(id, request),
                ErrorCode.LESSON_NOT_FOUND.getMessage());
    }

    @Test
    void testFindAllByChapterIdSuccessfully() {
        List<Lesson> lessons = new ArrayList<>();
        lessons.add(new Lesson());

        when(lessonRepo.findAllByChapterId(anyLong())).thenReturn(lessons);

        List<Lesson> result = lessonService.findAllByChapterId(id);

        assertNotNull(result);
    }

    @Test
    void testFindAllByChapterIdFailed() {
        when(lessonRepo.findAllByChapterId(anyLong())).thenReturn(null);
        assertThrows(AppException.class,
                () -> lessonService.findAllByChapterId(id),
                ErrorCode.LESSON_NOT_FOUND.getMessage());
    }

    @Test
    void testFindAllSuccessfully() {
        List<Lesson> lessons = new ArrayList<>();
        lessons.add(new Lesson());

        when(lessonRepo.findAll()).thenReturn(lessons);

        List<Lesson> result = lessonService.findAll();

        assertNotNull(result);
    }

    @Test
    void testFindAllNotFound() {
        when(lessonRepo.findAll()).thenReturn(new ArrayList<>());
        assertEquals(0, lessonService.findAll().size());
    }
}
