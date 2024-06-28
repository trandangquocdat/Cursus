package com.fpt.cursus.service;


import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.service.impl.FirebaseStorageServiceImpl;
import com.google.cloud.storage.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FirebaseStorageServiceTest {

    @InjectMocks
    private FirebaseStorageServiceImpl firebaseStorageService;
    @Mock
    Storage storage;

    @Mock
    MultipartFile multipartFile;

    @Mock
    Blob blob;

    @Mock
    private StorageOptions.Builder storageOptionsBuilder;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        //given
        multipartFile = new MockMultipartFile("file",
                "test.txt",
                "text/plain",
                "Hello World".getBytes());
        setField(firebaseStorageService, "bucketName", "cursus-b6cde.appspot.com");
        setField(firebaseStorageService, "credentialsFilePath", "./firebase-file-admin.json");
    }

    private void setField(Object targetObject, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = targetObject.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(targetObject, value);
    }

    @Test
    void testInitializeStorageSuccess() throws Exception {
        //when
        Method initializeStorageMethod = FirebaseStorageServiceImpl.class
                .getDeclaredMethod("initializeStorage");
        initializeStorageMethod.setAccessible(true);
        initializeStorageMethod.invoke(firebaseStorageService);
        //then
        assertNotNull(storage);
    }

    @Test
    void testUploadFile_InputStreamException() throws IOException {
        //given
        String fileName = "testFile.txt";
        MultipartFile file = mock(MultipartFile.class);
        //when
        when(file.getOriginalFilename()).thenReturn(fileName);
        when(file.getContentType()).thenReturn("text/plain");
        when(file.getInputStream()).thenReturn(null);
        //then
        assertThrows(StorageException.class,
                () -> firebaseStorageService.uploadFile(file));
    }

    @Test
    void testUploadFile_Success() throws IOException {
        //when
        String result = firebaseStorageService.uploadFile(multipartFile);
        //then
        assertTrue(result.contains(Objects.requireNonNull(
                multipartFile.getOriginalFilename()
        )));
    }

    @Test
    void testUploadFile_StorageException() {
        //when
        doThrow(StorageException.class)
                .when(storage)
                .create(any(BlobInfo.class), any(InputStream.class));
        //then
        IOException exception = assertThrows(IOException.class,
                () -> firebaseStorageService.uploadFile(multipartFile));
        assertNotNull(exception.getCause());
        assertInstanceOf(StorageException.class, exception.getCause());
    }

    @Test
    void testDownloadFileAsResponse_FileNotFoundException() {
        //given
        String filename = "test.txt";
        String bucketName = "cursus-b6cde.appspot.com";
        //then
        assertThrows(AppException.class,
                () -> firebaseStorageService.downloadFileAsResource(bucketName, filename),
                ErrorCode.FILE_NOT_FOUND.getMessage());
    }

    @Test
    void testDownloadFileAsResponse_Success() {
        //given
        String fileName = "testFile.txt";
        String bucketName = "cursus-b6cde.appspot.com";
        byte[] content = "test content".getBytes();
        //when
        when(storage.get(anyString(), anyString())).thenReturn(blob);
        when(blob.getContent()).thenReturn(content);
        Resource resource = firebaseStorageService.downloadFileAsResource(bucketName, fileName);
        //then
        assertNotNull(resource);
        assertEquals(content, ((ByteArrayResource) resource).getByteArray());
    }

    @Test
    void testDownloadFileAsBytes_FileNotFoundException() {
        //given
        String filename = "test.txt";
        String bucketName = "cursus-b6cde.appspot.com";
        //then
        assertThrows(AppException.class,
                () -> firebaseStorageService.downloadFileAsBytes(bucketName, filename),
                ErrorCode.FILE_NOT_FOUND.getMessage());
    }

    @Test
    void testDownloadFileAsBytes_Success() {
        //given
        String fileName = "testFile.txt";
        String bucketName = "cursus-b6cde.appspot.com";
        byte[] content = "test content".getBytes();
        //when
        when(storage.get(anyString(), anyString())).thenReturn(blob);
        when(blob.getContent()).thenReturn(content);
        byte[] downloadedContent = firebaseStorageService.downloadFileAsBytes(bucketName, fileName);
        //then
        assertNotNull(downloadedContent);
        assertEquals(content, downloadedContent);
    }
}
