//package com.fpt.cursus.service;
//
//
//import com.fpt.cursus.entity.Account;
//import com.fpt.cursus.entity.Course;
//import com.fpt.cursus.entity.Lesson;
//import com.fpt.cursus.exception.exceptions.AppException;
//import com.fpt.cursus.exception.exceptions.ErrorCode;
//import com.fpt.cursus.service.impl.FileServiceImpl;
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.cloud.storage.*;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.core.io.Resource;
//import org.springframework.mock.web.MockMultipartFile;
//
//import java.io.IOException;
//import java.lang.reflect.Field;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.Objects;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class FileServiceTest {
//
//    @InjectMocks
//    private FileServiceImpl fileService;
//
//    @Mock
//    private AccountService accountService;
//
//    @Mock
//    private CourseService courseService;
//
//    @Mock
//    private LessonService lessonService;
//
//    @Mock
//    private MockMultipartFile multipartFile;
//
//    private String link;
//
//    @Mock
//    private Storage storage;
//
//    @BeforeEach
//    void setUp() throws NoSuchFieldException,
//            IllegalAccessException,
//            InvocationTargetException,
//            NoSuchMethodException {
//        //given
//        setField(fileService,
//                "bucketName",
//                "cursus-b6cde.appspot.com");
//        setField(fileService,
//                "credentialsFilePath",
//                "./firebase-file-admin.json");
//
//        setMethod(fileService);
//
//        multipartFile = new MockMultipartFile("file",
//                "test.txt",
//                "text/plain",
//                "Hello World".getBytes());
//
//        link = "https://firebasestorage.googleapis.com/v0/b/cursus-b6cde.appspot.com/o/";
//    }
//
//    private void setField(Object targetObject, String fieldName, Object value)
//            throws NoSuchFieldException, IllegalAccessException {
//        Field field = targetObject.getClass().getDeclaredField(fieldName);
//        field.setAccessible(true);
//        field.set(targetObject, value);
//    }
//
//    private void setMethod(Object targetObject) throws NoSuchMethodException,
//            InvocationTargetException,
//            IllegalAccessException {
//        Method method = targetObject.getClass().getDeclaredMethod("initializeStorage");
//        method.setAccessible(true);
//        method.invoke(targetObject);
//    }
//
//    @Test
//    void uploadFileSuccess() throws IOException {
//        //then
//        String result = fileService.uploadFile(multipartFile);
//        assertTrue(result.contains(Objects.requireNonNull(
//                multipartFile.getOriginalFilename()
//        )));
//    }
//
//    @Test
//    void setAvatarSuccess() {
//        //given
//        Account account = new Account();
//        //then
//        fileService.setAvatar(multipartFile, account);
//        assertTrue(account.getAvatar().contains(link));
//    }
//
//    @Test
//    void setPictureSuccess() {
//        //given
//        Course course = new Course();
//        //then
//        fileService.setPicture(multipartFile, course);
//        assertTrue(course.getPictureLink().contains(link));
//    }
//
//    @Test
//    void setVideoSuccess() {
//        //given
//        Lesson lesson = new Lesson();
//        //then
//        fileService.setVideo(multipartFile, lesson);
//        assertTrue(lesson.getVideoLink().contains(link));
//    }
//
//    @Test
//    void downloadFileAsResourceFailed() {
//        //given
//        String bucketName = "cursus-b6cde.appspot.com";
//        //when
//        //then
//        AppException exception = assertThrows(AppException.class,
//                () -> fileService.downloadFileAsResource(bucketName, "testFile.txt"));
//        assertEquals(ErrorCode.FILE_NOT_FOUND, exception.getErrorCode());
//    }
//
//    @Test
//    void downloadFileAsBytesFailed() {
//        //given
//        String bucketName = "cursus-b6cde.appspot.com";
//        //when
//        //then
//        AppException exception = assertThrows(AppException.class,
//                () -> fileService.downloadFileAsBytes(bucketName, "testFile.txt"));
//        assertEquals(ErrorCode.FILE_NOT_FOUND, exception.getErrorCode());
//    }
//
//    @Test
//    void initializeStorageFailed() throws NoSuchFieldException,
//            IllegalAccessException {
//        //given
//        setField(fileService, "credentialsFilePath", "non");
//        //when
//        //then
//        assertThrows(IOException.class,
//                () -> setMethod(fileService),
//                ErrorCode.STORAGE_INITIALIZE_FAIL.getMessage());
//    }
//}
