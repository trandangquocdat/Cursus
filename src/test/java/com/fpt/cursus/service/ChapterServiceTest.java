package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.CreateChapterRequest;
import com.fpt.cursus.dto.request.UpdateChapterDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Chapter;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.enums.ChapterStatus;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.ChapterRepo;
import com.fpt.cursus.service.impl.ChapterServiceImpl;
import com.fpt.cursus.util.AccountUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import javax.lang.model.element.UnknownAnnotationValueException;
import java.util.Optional;
import java.util.*;

class ChapterServiceTest {

    @Mock
    private ChapterRepo chapterRepo;

    @Mock
    private CourseService courseService;

    @Mock
    private AccountUtil accountUtil;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ChapterServiceImpl chapterService;

    private Chapter chapter;
    private UpdateChapterDto updateChapterDto;
    private Account account;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        chapter = new Chapter();
        chapter.setId(1L);
        chapter.setStatus(ChapterStatus.ACTIVE);
        chapter.setCreatedDate(new Date());
        chapter.setCreatedBy("user");

        updateChapterDto = new UpdateChapterDto();
        updateChapterDto.setName("Updated Chapter");

        account = new Account();
        account.setUsername("user");

        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(chapterRepo.findById(anyLong())).thenReturn(Optional.of(chapter));
        doAnswer(invocation -> {
            Chapter c = invocation.getArgument(1);
            c.setName(updateChapterDto.getName());
            return null;
        }).when(modelMapper).map(any(UpdateChapterDto.class), any(Chapter.class));
        when(chapterRepo.save(any(Chapter.class))).thenReturn(chapter);
    }

    @Test
    public void testUpdateChapter_Success() {
        //then
        Chapter updatedChapter = chapterService.updateChapter(1L, updateChapterDto);

        verify(chapterRepo, times(1)).findById(1L);
        verify(modelMapper, times(1)).getConfiguration();
        verify(modelMapper, times(1)).map(updateChapterDto, chapter);
        verify(chapterRepo, times(1)).save(chapter);

        assertEquals(updateChapterDto.getName(), updatedChapter.getName());
        assertEquals(account.getUsername(), updatedChapter.getUpdatedBy());
    }

    @Test
    void testCreateChapter_HappyPath() {
        //given
        Long courseId = 1L;
        CreateChapterRequest request = new CreateChapterRequest();
        Course course = new Course();
        course.setId(courseId);
        when(courseService.getCourseById(courseId)).thenReturn(course);

        Account account = new Account();
        account.setUsername("testUser");
        when(accountUtil.getCurrentAccount()).thenReturn(account);

        Chapter chapter = new Chapter();

        //when
        when(modelMapper.map(request, Chapter.class)).thenReturn(chapter);
        when(chapterRepo.save(any(Chapter.class))).thenReturn(chapter);
        Chapter createdChapter = chapterService.createChapter(courseId, request);

        //then
        assertNotNull(createdChapter);
        assertEquals(course, createdChapter.getCourse());
        assertEquals(ChapterStatus.ACTIVE, createdChapter.getStatus());
        assertEquals("testUser", createdChapter.getCreatedBy());
        verify(chapterRepo, times(1)).save(any(Chapter.class));
    }

    @Test
    void testCreateChapter_CourseNotFound() {
        //given
        Long courseId = 1L;
        CreateChapterRequest request = new CreateChapterRequest();

        //when
        when(courseService.getCourseById(courseId)).thenThrow(new AppException(ErrorCode.COURSE_NOT_FOUND));

        //then
        assertThrows(AppException.class, () -> chapterService.createChapter(courseId, request));
        verify(courseService, times(1)).getCourseById(courseId);
    }

    @Test
    void testDeleteChapterById_HappyPath() {
        //given
        Long chapterId = 1L;
        Chapter chapter = new Chapter();

        //when
        when(chapterRepo.findById(chapterId)).thenReturn(Optional.of(chapter));
        when(chapterRepo.save(any(Chapter.class))).thenReturn(chapter);

        Chapter deletedChapter = chapterService.deleteChapterById(chapterId);

        //then
        assertNotNull(deletedChapter);
        assertEquals(ChapterStatus.DELETED, deletedChapter.getStatus());
        assertNull(deletedChapter.getCourse());
        verify(chapterRepo, times(1)).save(any(Chapter.class));
    }

    @Test
    void testDeleteChapterById_ChapterNotFound() {
        //given
        Long chapterId = 1L;

        //when
        when(chapterRepo.findById(chapterId)).thenReturn(Optional.empty());

        //then
        assertThrows(AppException.class, () -> chapterService.deleteChapterById(chapterId));
        verify(chapterRepo, times(1)).findById(chapterId);
    }

    @Test
    void testUpdateChapter_ChapterNotFound() {
        //given
        Long chapterId = 1L;
        UpdateChapterDto request = new UpdateChapterDto();

        //when
        when(chapterRepo.findById(chapterId)).thenReturn(Optional.empty());

        //then
        assertThrows(AppException.class, () -> chapterService.updateChapter(chapterId, request));
        verify(chapterRepo, times(1)).findById(chapterId);
    }

    @Test
    void testFindChapterById_HappyPath() {
        //given
        Long chapterId = 1L;
        Chapter chapter = new Chapter();
        chapter.setId(chapterId);  // Đảm bảo gán giá trị cho ID

        //when
        when(chapterRepo.findById(chapterId)).thenReturn(Optional.of(chapter));
        Chapter foundChapter = chapterService.findChapterById(chapterId);

        //then
        assertNotNull(foundChapter);
        assertEquals(chapterId, foundChapter.getId());  // Kiểm tra giá trị ID
        verify(chapterRepo, times(1)).findById(chapterId);
    }

    @Test
    void testFindChapterById_ChapterNotFound() {
        //given
        Long chapterId = 1L;

        //when
        when(chapterRepo.findById(chapterId)).thenReturn(Optional.empty());

        //then
        assertThrows(AppException.class, () -> chapterService.findChapterById(chapterId));
        verify(chapterRepo, times(1)).findById(chapterId);
    }

    @Test
    void testFindAll_HappyPath() {
        //given
        List<Chapter> chapters = Arrays.asList(new Chapter(), new Chapter());

        //when
        when(chapterRepo.findAll()).thenReturn(chapters);
        List<Chapter> foundChapters = chapterService.findAll();

        //then
        assertNotNull(foundChapters);
        assertEquals(chapters.size(), foundChapters.size());
        verify(chapterRepo, times(1)).findAll();
    }

    @Test
    void testFindAllByCourseId_HappyPath() {
        //given
        Long courseId = 1L;
        List<Chapter> chapters = Arrays.asList(new Chapter(), new Chapter());

        //when
        when(chapterRepo.findAllByCourseId(courseId)).thenReturn(chapters);
        List<Chapter> foundChapters = chapterService.findAllByCourseId(courseId);

        //then
        assertNotNull(foundChapters);
        assertEquals(chapters.size(), foundChapters.size());
        verify(chapterRepo, times(1)).findAllByCourseId(courseId);
    }

    @Test
    void testFindAllByCourseId_ChaptersNotFound() {
        //given
        Long courseId = 1L;

        //when
        when(chapterRepo.findAllByCourseId(courseId)).thenReturn(Collections.emptyList());

        //then
        assertThrows(AppException.class, () -> chapterService.findAllByCourseId(courseId));
        verify(chapterRepo, times(1)).findAllByCourseId(courseId);
    }
}
