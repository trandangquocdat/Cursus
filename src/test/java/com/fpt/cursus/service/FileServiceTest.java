package com.fpt.cursus.service;


import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.service.impl.FileServiceImpl;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockMultipartFile;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @InjectMocks
    private FileServiceImpl fileService;

    @Mock
    private AccountService accountService;

    @Mock
    private CourseService courseService;

    @Mock
    private LessonService lessonService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private MockMultipartFile multipartFile;

    private String link;

    @Mock
    private Storage storage;

    @Mock
    private Blob blob;

    @Mock
    private WriteChannel writeChannel;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        //given
        setField(fileService,
                "bucketName",
                "test-bucket");
        setField(fileService,
                "credentialsFilePath",
                "./firebase-file-admin.json");

        multipartFile = new MockMultipartFile("file",
                "test.txt",
                "text/plain",
                "Hello World".getBytes());

        link = String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/", "test-bucket");
    }

    private void setField(Object targetObject, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = targetObject.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(targetObject, value);
    }

    private void setMethod(Object targetObject)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = targetObject.getClass().getDeclaredMethod("initializeStorage");
        method.setAccessible(true);
        method.invoke(targetObject);
    }
//
//    @Test
//    void testSetAvatarSuccess() throws NoSuchFieldException, IllegalAccessException {
//        //given
//        Account account = new Account();
//        setField(fileService, "storage", storage);
//        //when
//        when(storage.writer(any(BlobInfo.class))).thenReturn(writeChannel);
//        //then
//        fileService.setAvatar(multipartFile, account);
//        assertTrue(account.getAvatar().contains(link));
//    }
//
//    @Test
//    void testSetPictureSuccess() throws NoSuchFieldException, IllegalAccessException {
//        //given
//        Course course = new Course();
//        setField(fileService, "storage", storage);
//        //when
//        when(storage.writer(any(BlobInfo.class))).thenReturn(writeChannel);
//        //then
//        fileService.setPicture(multipartFile, course);
//        assertTrue(course.getPictureLink().contains(link));
//    }
//
//    @Test
//    void testSetVideoSuccess() throws NoSuchFieldException, IllegalAccessException {
//        //given
//        Lesson lesson = new Lesson();
//        setField(fileService, "storage", storage);
//        //when
//        when(storage.writer(any(BlobInfo.class))).thenReturn(writeChannel);
//        //then
//        fileService.setVideo(multipartFile, lesson);
//        assertTrue(lesson.getVideoLink().contains(link));
//    }
//
//    @Test
//    void testSetAvatarFail() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
//        //given
//        Account account = new Account();
//        setMethod(fileService);
//        //then
//        assertThrows(AppException.class, () -> fileService.setAvatar(multipartFile, account));
//    }
//
//    @Test
//    void testSetPictureFail() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
//        //given
//        Course course = new Course();
//        setMethod(fileService);
//        //then
//        assertThrows(AppException.class, () -> fileService.setPicture(multipartFile, course));
//    }
//
//    @Test
//    void testSetVideoFail() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
//        //given
//        Lesson lesson = new Lesson();
//        setMethod(fileService);
//        //then
//        assertThrows(AppException.class, () -> fileService.setVideo(multipartFile, lesson));
//    }
//
//    @Test
//    void testUploadFileFailRuntimeException() throws NoSuchFieldException, IllegalAccessException {
//        //given
//        setField(fileService, "storage", storage);
//        //when
//        //then
//        assertThrows(RuntimeException.class, () -> fileService.uploadFile(multipartFile));
//    }

    @Test
    void testDownloadFileAsResourceSuccess() throws NoSuchFieldException, IllegalAccessException {
        //given
        setField(fileService, "storage", storage);
        byte[] content = "content".getBytes();
        //when
        when(storage.get(anyString(), anyString())).thenReturn(blob);
        when(blob.getContent()).thenReturn(content);
        //then
        Resource result = fileService.downloadFileAsResource("test.bucket", "test.txt");
        assertNotNull(result);
    }

    @Test
    void testDownloadFileAsResourceFail() throws NoSuchFieldException, IllegalAccessException {
        //given
        setField(fileService, "storage", storage);
        //when
        when(storage.get(anyString(), anyString())).thenReturn(null);
        //then
        assertThrows(AppException.class, () -> fileService.downloadFileAsResource("test.bucket", "test.txt"));
    }

    @Test
    void testDownloadFileAsBytesSuccess() throws NoSuchFieldException, IllegalAccessException {
        //given
        setField(fileService, "storage", storage);
        byte[] content = "content".getBytes();
        //when
        when(storage.get(anyString(), anyString())).thenReturn(blob);
        when(blob.getContent()).thenReturn(content);
        //then
        byte[] result = fileService.downloadFileAsBytes("test.bucket", "test.txt");
        assertNotNull(result);
    }

    @Test
    void testDownloadFileAsBytesFail() throws NoSuchFieldException, IllegalAccessException {
        //given
        setField(fileService, "storage", storage);
        //when
        when(storage.get(anyString(), anyString())).thenReturn(null);
        //then
        assertThrows(AppException.class, () -> fileService.downloadFileAsBytes("test.bucket", "test.txt"));
    }

}
