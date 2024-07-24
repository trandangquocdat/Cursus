package com.fpt.cursus.service;


import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.service.impl.FileServiceImpl;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
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
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Mock
    private Storage storage;

    @Mock
    private Blob blob;

    @Mock
    private WriteChannel writeChannel;

    @Mock
    private URL url;

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

    @Test
    void testUploadFileFailRuntimeException() throws NoSuchFieldException, IllegalAccessException {
        //given
        setField(fileService, "storage", storage);
        //when
        //then
        assertThrows(RuntimeException.class, () -> fileService.uploadFile(multipartFile, "test.txt"));
    }

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

    @Test
    void testLinkSaveSuccess() throws IllegalAccessException, NoSuchFieldException {
        //given
        setField(fileService, "storage", storage);
        String folder = "test";
        String originalFileName = "test.txt";
        //when
        when(storage.writer(any(BlobInfo.class))).thenReturn(writeChannel);
        //then
        String result = fileService.linkSave(multipartFile, folder);
        assertTrue(result.contains(originalFileName));
        assertTrue(result.contains(folder));
    }

    @Test
    void testLinkSaveFail() throws IllegalAccessException, NoSuchFieldException {
        //given
        setField(fileService, "storage", storage);
        //when
        when(storage.writer(any(BlobInfo.class))).thenThrow(StorageException.class);
        //then
        assertThrows(AppException.class, () -> fileService.linkSave(multipartFile, "test"));
    }

    @Test
    void testGetSignedImageUrl() throws NoSuchFieldException, IllegalAccessException {
        //given
        String filename = "test.txt";
        setField(fileService, "storage", storage);
        //when
        when(storage.signUrl(any(BlobInfo.class), any(Long.class), any(), any())).thenReturn(url);
        //then
        fileService.getSignedImageUrl(filename);
        verify(storage, times(1)).signUrl(any(BlobInfo.class), any(Long.class), any(), any());
    }

    @Test
    void testInitializeStorage() {
        //then
        assertDoesNotThrow(() -> setMethod(fileService));
    }
}
