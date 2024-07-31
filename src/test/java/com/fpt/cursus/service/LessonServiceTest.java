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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

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
        mockNonVideo = new MockMultipartFile("video", "testing.txt", "text/plain", "".getBytes());
        chapterId = 1L;
        lessonId = 1L;
        lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setChapter(new Chapter());
        lesson.setStatus(LessonStatus.ACTIVE);
        lesson.setCreatedBy(mockAccount.getUsername());
    }

    @Test
    void uploadLessonFromExcel_Success() throws IOException {

        File file = File.createTempFile("test", ".txt");
        file.deleteOnExit();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue(file.getAbsolutePath());
        row.createCell(1).setCellValue("Test Lesson");
        row.createCell(2).setCellValue("This is a test lesson");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        byte[] excelBytes = outputStream.toByteArray();

        MockMultipartFile excelFile = new MockMultipartFile("file",
                "example.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new ByteArrayInputStream(excelBytes));

        when(accountUtil.getCurrentAccount()).thenReturn(mockAccount);
        when(fileUtil.isVideo(any(MultipartFile.class))).thenReturn(true);
        when(fileService.linkSave(any(MultipartFile.class), anyString())).thenReturn("uploaded/link");

        List<String> result = lessonService.uploadLessonFromExcel(chapterId, excelFile);

        assertNotNull(result);
        verify(lessonRepo, times(1)).save(any(Lesson.class));
        verify(fileService, times(1)).linkSave(any(MultipartFile.class), anyString());
    }

    @Test
    void uploadLessonFromExcel_NotVideo() throws IOException {

        File file = File.createTempFile("test", ".txt");
        file.deleteOnExit();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue(file.getAbsolutePath());
        row.createCell(1).setCellValue("Test Lesson");
        row.createCell(2).setCellValue("This is a test lesson");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        byte[] excelBytes = outputStream.toByteArray();

        MockMultipartFile excelFile = new MockMultipartFile("file",
                "example.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new ByteArrayInputStream(excelBytes));

        when(accountUtil.getCurrentAccount()).thenReturn(mockAccount);
        when(fileUtil.isVideo(any(MultipartFile.class))).thenReturn(false);

        List<String> result = lessonService.uploadLessonFromExcel(chapterId, excelFile);

        assertNotNull(result);
        verify(lessonRepo, times(1)).save(any(Lesson.class));
        verify(fileService, never()).linkSave(any(MultipartFile.class), anyString());
    }

    @Test
    void uploadLessonFromExcel_FileNull() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("https://www.youtube.com/watch?v=123456");
        row.createCell(1).setCellValue("Test Lesson");
        row.createCell(2).setCellValue("This is a test lesson");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        byte[] excelBytes = outputStream.toByteArray();

        MockMultipartFile excelFile = new MockMultipartFile("file",
                "example.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new ByteArrayInputStream(excelBytes));

        when(accountUtil.getCurrentAccount()).thenReturn(mockAccount);

        List<String> result = lessonService.uploadLessonFromExcel(chapterId, excelFile);

        assertNotNull(result);
        verify(lessonRepo, times(1)).save(any(Lesson.class));
        verify(fileService, never()).linkSave(any(MultipartFile.class), anyString());
    }

    @Test
    void uploadLessonFromExcel_VideoLinkCellNull() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        Row row = sheet.createRow(0);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        byte[] excelBytes = outputStream.toByteArray();

        MockMultipartFile excelFile = new MockMultipartFile("file",
                "example.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new ByteArrayInputStream(excelBytes));
        ;

        when(accountUtil.getCurrentAccount()).thenReturn(mockAccount);

        List<String> result = lessonService.uploadLessonFromExcel(chapterId, excelFile);

        assertNotNull(result);
        verify(lessonRepo, never()).save(any(Lesson.class));
    }

    @Test
    void uploadLessonFromExcel_LessonNameCellNull() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("Column1");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        byte[] excelBytes = outputStream.toByteArray();

        MockMultipartFile excelFile = new MockMultipartFile("file",
                "example.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new ByteArrayInputStream(excelBytes));
        ;

        when(accountUtil.getCurrentAccount()).thenReturn(mockAccount);

        List<String> result = lessonService.uploadLessonFromExcel(chapterId, excelFile);

        assertNotNull(result);
        verify(lessonRepo, never()).save(any(Lesson.class));
    }

    @Test
    void uploadLessonFromExcel_InvalidVideo() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("Column1");
        row.createCell(1).setCellValue("Column2");

        Row row2 = sheet.createRow(1);
        row2.createCell(0).setCellValue("Value1");
        row2.createCell(1).setCellValue("Value2");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        byte[] excelBytes = outputStream.toByteArray();

        MockMultipartFile excelFile = new MockMultipartFile("file",
                "example.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new ByteArrayInputStream(excelBytes));
        ;

        when(accountUtil.getCurrentAccount()).thenReturn(mockAccount);

        List<String> result = lessonService.uploadLessonFromExcel(chapterId, excelFile);

        assertNotNull(result);
        verify(lessonRepo, never()).save(any(Lesson.class));
    }

    @Test
    void uploadLessonFromExcel_IOException() throws IOException {
        MultipartFile mockExcelFile = mock(MultipartFile.class);
        when(mockExcelFile.getInputStream()).thenThrow(new IOException("Failed to read Excel file"));

        when(accountUtil.getCurrentAccount()).thenReturn(mockAccount);

        IOException exception = assertThrows(IOException.class, () -> {
            lessonService.uploadLessonFromExcel(chapterId, mockExcelFile);
        });

        assertEquals("Failed to read Excel file", exception.getMessage());
    }

    @Test
    void testGetFileFromPath() throws IOException {
        File file = File.createTempFile("test", ".txt");
        file.deleteOnExit();

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            MockMultipartFile mockFile = new MockMultipartFile(file.getName(), file.getName(), "application/octet-stream", fileInputStream);

            MultipartFile result = lessonService.getFileFromPath(file.getAbsolutePath());

            assertNotNull(result);
            assertEquals(mockFile.getName(), result.getName());
        }
    }

    @Test
    void testGetFileFromPath_InvalidPath() {
        MultipartFile result = lessonService.getFileFromPath("invalid_path");

        assertNull(result);
    }

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
    void testCreateLesson_Null() {
        CreateLessonDto request = new CreateLessonDto();
        request.setVideoLink(null);
        lesson.setName("new lesson");

        Chapter chapter = new Chapter();
        chapter.setId(chapterId);

        when(chapterService.findChapterById(chapterId)).thenReturn(chapter);
        when(modelMapper.map(request, Lesson.class)).thenReturn(lesson);
        when(accountUtil.getCurrentAccount()).thenReturn(mockAccount);
        when(lessonRepo.save(any(Lesson.class))).thenReturn(lesson);

        Lesson awnserLesson = lessonService.createLesson(chapterId, request);

        assertNotNull(awnserLesson);
        assertEquals(chapterId, awnserLesson.getChapter().getId());
        assertEquals(lessonId, awnserLesson.getId());
        assertEquals(lesson.getName(), awnserLesson.getName());
        assertEquals(lesson.getStatus(), awnserLesson.getStatus());
        assertNull(awnserLesson.getVideoLink());
    }

    @Test
    void testFindLessonById() {
        when(lessonRepo.findById(lessonId)).thenReturn(Optional.ofNullable(lesson));

        Lesson foundLesson = lessonService.findLessonById(lessonId);

        assertNotNull(foundLesson);
        assertEquals(lessonId, foundLesson.getId());
    }

    @Test
    void testDeleteLessonById() {
        when(lessonRepo.findById(lessonId)).thenReturn(Optional.ofNullable(lesson));
        when(lessonRepo.save(any(Lesson.class))).thenReturn(lesson);

        Lesson deletedLesson = lessonService.deleteLessonById(lessonId);

        assertNotNull(deletedLesson);
        assertNull(deletedLesson.getChapter());
        assertEquals(LessonStatus.DELETED, deletedLesson.getStatus());
    }

    @Test
    void testUpdateLesson() {
        CreateLessonDto request = new CreateLessonDto();
        request.setVideoLink(mockVideo);

        when(lessonRepo.findById(lessonId)).thenReturn(Optional.ofNullable(lesson));
        when(fileUtil.isVideo(request.getVideoLink())).thenReturn(true);
        when(accountUtil.getCurrentAccount()).thenReturn(mockAccount);
        when(lessonRepo.save(any(Lesson.class))).thenReturn(lesson);

        Lesson updatedLesson = lessonService.updateLesson(lessonId, request);

        assertNotNull(updatedLesson);
        assertEquals(mockAccount.getUsername(), updatedLesson.getUpdatedBy());
    }

    @Test
    void testUpdateLessonFailed_NotVideo() {
        CreateLessonDto request = new CreateLessonDto();
        request.setVideoLink(mockNonVideo);

        when(lessonRepo.findById(lessonId)).thenReturn(Optional.ofNullable(lesson));
        when(fileUtil.isVideo(request.getVideoLink())).thenReturn(false);
        when(accountUtil.getCurrentAccount()).thenReturn(mockAccount);

        AppException exception = assertThrows(AppException.class, () -> {
            lessonService.updateLesson(lessonId, request);
        });

        assertEquals(ErrorCode.FILE_INVALID_VIDEO, exception.getErrorCode());
    }

    @Test
    void testUpdateLessonFailed_Null() {
        CreateLessonDto request = new CreateLessonDto();
        request.setVideoLink(null);

        when(lessonRepo.findById(lessonId)).thenReturn(Optional.ofNullable(lesson));
        when(accountUtil.getCurrentAccount()).thenReturn(mockAccount);
        when(lessonRepo.save(any(Lesson.class))).thenReturn(lesson);

        lessonService.updateLesson(lessonId, request);
    }

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
