package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.CreateLessonDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Chapter;
import com.fpt.cursus.entity.Lesson;
import com.fpt.cursus.enums.LessonStatus;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.LessonRepo;
import com.fpt.cursus.service.impl.LessonServiceImpl;
import com.fpt.cursus.util.AccountUtil;
import com.fpt.cursus.util.FileUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LessonServiceTest {

    @Mock
    private LessonRepo lessonRepo;

    @Mock
    private ChapterService chapterService;

    @Mock
    private AccountUtil accountUtil;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private FileUtil fileUtil;

    @InjectMocks
    private LessonServiceImpl lessonService;

    @Mock
    private FileService fileService;

    private Account mockAccount;
    private MultipartFile mockVideo;
    private MultipartFile mockNonVideo;
    private Long chapterId;
    private Long lessonId;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        mockAccount = new Account();
        mockAccount.setUsername("testUser");
        mockVideo = new MockMultipartFile("video", "video.mp4", "video/mp4", "".getBytes());
        mockNonVideo = new MockMultipartFile("video", "testfile.txt", "text/plain", "".getBytes());
        chapterId = 1L;
        lessonId = 1L;
        lesson = new Lesson();
        lesson.setId(lessonId);
    }

//    private MultipartFile invokeGetFileFromPath(Object targetObject, String filePath) throws NoSuchMethodException,
//            InvocationTargetException, IllegalAccessException {
//        Method method = targetObject.getClass().getDeclaredMethod("getFileFromPath", String.class);
//        method.setAccessible(true);
//        return (MultipartFile) method.invoke(targetObject, filePath);
//    }

    @Test
    void testCreateLesson() {
        Long chapterId = 1L;
        CreateLessonDto request = new CreateLessonDto();
        request.setVideoLink(mockVideo);

        Chapter chapter = new Chapter();
        chapter.setId(chapterId);

        when(chapterService.findChapterById(chapterId)).thenReturn(chapter);
        when(accountUtil.getCurrentAccount()).thenReturn(mockAccount);
        when(fileUtil.isVideo(request.getVideoLink())).thenReturn(true);

        Lesson lesson = new Lesson();
        when(modelMapper.map(request, Lesson.class)).thenReturn(lesson);
        when(lessonRepo.save(any(Lesson.class))).thenReturn(lesson);

        Lesson createdLesson = lessonService.createLesson(chapterId, request);

        assertNotNull(createdLesson);
        assertEquals(chapter, createdLesson.getChapter());
        assertEquals(mockAccount.getUsername(), createdLesson.getCreatedBy());
        assertEquals(LessonStatus.ACTIVE, createdLesson.getStatus());
    }

    @Test
    void testCreateLessonInvalidVideo() {
        CreateLessonDto request = new CreateLessonDto();
        request.setVideoLink(mockNonVideo);
        lesson.setName("new lesson");

        when(modelMapper.map(request, Lesson.class)).thenReturn(lesson);
        when(accountUtil.getCurrentAccount()).thenReturn(mockAccount);
        when(fileUtil.isVideo(request.getVideoLink())).thenReturn(false);

        AppException exception = assertThrows(AppException.class, () -> {
            lessonService.createLesson(chapterId, request);
        });

        assertEquals(ErrorCode.FILE_INVALID_VIDEO, exception.getErrorCode());
    }

    @Test
    void testFindLessonById() {
        when(lessonRepo.findLessonById(lessonId)).thenReturn(lesson);

        Lesson foundLesson = lessonService.findLessonById(lessonId);

        assertNotNull(foundLesson);
        assertEquals(lessonId, foundLesson.getId());
    }

    @Test
    void testDeleteLessonById() {
        lesson.setChapter(new Chapter());

        when(lessonRepo.findLessonById(lessonId)).thenReturn(lesson);
        when(lessonRepo.save(any(Lesson.class))).thenReturn(lesson);

        Lesson deletedLesson = lessonService.deleteLessonById(lessonId);

        assertNotNull(deletedLesson);
        assertNull(deletedLesson.getChapter());
        assertEquals(LessonStatus.DELETED, deletedLesson.getStatus());
    }

    @Test
    void testUpdateLesson() {
        Long lessonId = 1L;
        CreateLessonDto request = new CreateLessonDto();
        request.setVideoLink(mockVideo);

        Lesson existingLesson = new Lesson();
        existingLesson.setId(lessonId);

        when(lessonRepo.findLessonById(lessonId)).thenReturn(existingLesson);
        when(fileUtil.isVideo(request.getVideoLink())).thenReturn(true);
        when(accountUtil.getCurrentAccount()).thenReturn(mockAccount);
        when(lessonRepo.save(any(Lesson.class))).thenReturn(existingLesson);

        Lesson updatedLesson = lessonService.updateLesson(lessonId, request);

        assertNotNull(updatedLesson);
        assertEquals(mockAccount.getUsername(), updatedLesson.getUpdatedBy());
    }

    @Test
    void testUpdateLessonFailed() {
        CreateLessonDto request = new CreateLessonDto();
        request.setVideoLink(mockNonVideo);

        Lesson existingLesson = new Lesson();
        existingLesson.setId(lessonId);

        when(lessonRepo.findLessonById(lessonId)).thenReturn(existingLesson);
        when(fileUtil.isVideo(request.getVideoLink())).thenReturn(false);

        AppException exception = assertThrows(AppException.class, () -> {
            lessonService.updateLesson(lessonId, request);
        });

        assertEquals(ErrorCode.FILE_INVALID_VIDEO, exception.getErrorCode());
    }

//    @Test
//    void testUploadLessonFromExcel() throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
//        byte[] excelBytes;
//        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
//            Sheet sheet = workbook.createSheet("Sheet1");
//            Row headerRow = sheet.createRow(0);
//            headerRow.createCell(0).setCellValue("videoLink");
//            headerRow.createCell(1).setCellValue("lessonName");
//            headerRow.createCell(2).setCellValue("description");
//
//            Row dataRow = sheet.createRow(1);
//            dataRow.createCell(0).setCellValue("video.mp4");
//            dataRow.createCell(1).setCellValue("Lesson 1");
//            dataRow.createCell(2).setCellValue("Description 1");
//
//            workbook.write(out);
//            excelBytes = out.toByteArray();
//        }
//
//        MultipartFile excelFile = new MockMultipartFile(
//                "file",
//                "test.xlsx",
//                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
//                new ByteArrayInputStream(excelBytes)
//        );
//
//        MockMultipartFile videoFile = new MockMultipartFile(
//                "file",
//                "video.mp4",
//                "video/mp4",
//                "This is a video file content".getBytes()
//        );
//
//
//        when(invokeGetFileFromPath(lessonService, "video.mp4")).thenReturn(videoFile);
//        when(fileUtil.isVideo(any(MultipartFile.class))).thenReturn(true);
//        when(fileService.uploadFile(any(MultipartFile.class))).thenReturn("uploadedFileUrl");
//        when(lessonRepo.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        List<String> uploadedFileUrls = lessonService.uploadLessonFromExcel(chapterId, excelFile);
//
//        assertNotNull(uploadedFileUrls);
//        assertEquals(1, uploadedFileUrls.size());
//        assertEquals("uploadedFileUrl", uploadedFileUrls.get(0));
//    }

    @Test
    void testFindAllByChapterId() {
        List<Lesson> lessons = new ArrayList<>();
        lesson.setId(1L);
        lessons.add(lesson);

        when(lessonRepo.findAllByChapterId(chapterId)).thenReturn(lessons);

        List<Lesson> foundLessons = lessonService.findAllByChapterId(chapterId);

        assertNotNull(foundLessons);
        assertEquals(1, foundLessons.size());
        assertEquals(lesson.getId(), foundLessons.get(0).getId());
    }

    @Test
    void testFindAllByChapterId_NotFound() {
        when(lessonRepo.findAllByChapterId(chapterId)).thenReturn(null);

        AppException exception = assertThrows(AppException.class, () -> {
            lessonService.findAllByChapterId(chapterId);
        });

        assertEquals(ErrorCode.LESSON_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testFindAll() {
        List<Lesson> lessons = new ArrayList<>();
        lesson.setId(1L);
        lessons.add(lesson);

        when(lessonRepo.findAll()).thenReturn(lessons);

        List<Lesson> foundLessons = lessonService.findAll();

        assertNotNull(foundLessons);
        assertEquals(1, foundLessons.size());
        assertEquals(lesson.getId(), foundLessons.get(0).getId());
    }

    @Test
    void testSave() {
        lesson.setId(1L);

        when(lessonRepo.save(any(Lesson.class))).thenReturn(lesson);

        lessonService.save(lesson);

        verify(lessonRepo, times(1)).save(lesson);
    }
}
