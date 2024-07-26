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
import org.apache.poi.ss.usermodel.Cell;
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    private MultipartFile createExcelFile(String[][] data) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();

        for (int i = 0; i < data.length; i++) {
            Row row = sheet.createRow(i);
            for (int j = 0; j < data[i].length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(data[i][j]);
            }
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        when(accountUtil.getCurrentAccount()).thenReturn(mockAccount);
        when(chapterService.findChapterById(chapterId)).thenReturn(new Chapter());

        return new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bos.toByteArray());
    }

    @Test
    public void testUploadLessonFromExcel_success() throws Exception {
        String[][] excelContent = {
                {"https://example.com/video1.mp4", "Lesson 1", "Description 1"}
        };
        MultipartFile excelFile = createExcelFile(excelContent);

        when(fileUtil.isVideo(any(MultipartFile.class))).thenReturn(true);
        when(fileService.setVideo(any(MultipartFile.class), any(Lesson.class))).thenAnswer(invocation -> {
            Lesson lesson = invocation.getArgument(1);
            lesson.setVideoLink("https://example.com/video1.mp4");
            return null;
        });

        // Mock getFileFromPath to return a mock video file
        LessonServiceImpl lessonServiceSpy = spy(lessonService);
        doReturn(mockVideo).when(lessonServiceSpy).getFileFromPath(anyString());

        List<String> uploadedFileUrls = lessonServiceSpy.uploadLessonFromExcel(chapterId, excelFile);

        assertNotNull(uploadedFileUrls);
        verify(lessonRepo, times(1)).save(any(Lesson.class));
    }

//    @Test
//    public void testUploadLessonFromExcel_invalidVideoLink() throws Exception {
//        String[][] excelContent = {
//                {"invalid_path", "Lesson 1", "Description 1"}
//        };
//        MultipartFile excelFile = createExcelFile(excelContent);
//
//        when(fileUtil.isVideo(any(MultipartFile.class))).thenReturn(false);
//
//        lessonService.uploadLessonFromExcel(chapterId, excelFile);
//    }
//
//    @Test
//    public void testUploadLessonFromExcel_nullCells() throws Exception {
//        String[][] excelContent = {
//                {"", "", ""}
//        };
//        MultipartFile excelFile = createExcelFile(excelContent);
//
//        List<String> uploadedFileUrls = lessonService.uploadLessonFromExcel(chapterId, excelFile);
//
//        assertNotNull(uploadedFileUrls);
//        verify(lessonRepo, times(0)).save(any(Lesson.class));
//    }

    @Test
    public void testUploadLessonFromExcel_fileIOException() {
        MultipartFile excelFile = mock(MultipartFile.class);

        try {
            when(excelFile.getInputStream()).thenThrow(new IOException("Test exception"));

            IOException thrown = assertThrows(IOException.class, () -> {
                lessonService.uploadLessonFromExcel(chapterId, excelFile);
            });

            assertEquals("Failed to read Excel file", thrown.getMessage());
        } catch (IOException e) {
            fail("Unexpected exception thrown");
        }
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
    void testUpdateLessonFailed_NotVideo() {
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

    @Test
    void testUpdateLessonFailed_Null() {
        CreateLessonDto request = new CreateLessonDto();
        request.setVideoLink(null);

        Lesson existingLesson = new Lesson();
        existingLesson.setId(lessonId);

        when(lessonRepo.findLessonById(lessonId)).thenReturn(existingLesson);
        when(accountUtil.getCurrentAccount()).thenReturn(mockAccount);

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
