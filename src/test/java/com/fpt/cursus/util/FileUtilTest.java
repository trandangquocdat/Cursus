package com.fpt.cursus.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {FileUtil.class})
class FileUtilTest {

    @Mock
    private MultipartFile mockMultipartFile;

    @InjectMocks
    private FileUtil fileUtil;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getFileFromPathTest() throws IOException {

        //Given
        File mockFile = File.createTempFile("TestFile", ".txt");
        try (FileOutputStream out = new FileOutputStream(mockFile);) {
            out.write("Test Content".getBytes());

            //When
            MultipartFile result = fileUtil.getFileFromPath(mockFile.getPath());

            //Then
            assertEquals(mockFile.getName(), result.getName());
            assertEquals("application/octet-stream", result.getContentType());
            assertEquals("Test Content", new String(result.getBytes()));
            mockFile.delete();
        }
    }

    @Test
    void testGetFileFromPathNul() {
        //Given
        String filePath = "test.txt";
        //When
        MultipartFile result = fileUtil.getFileFromPath(filePath);
        //Then
        assertNull(result);
    }

    @Test
    void isImage() throws IOException {

        //Given
        InputStream inputStream = new ByteArrayInputStream(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        MultipartFile testFile = new MockMultipartFile("TestFile", "TestFile.jpeg", "image/jpeg", inputStream);

        //When
        boolean result = fileUtil.isImage(testFile);

        //Then
        assertTrue(result);

    }

    @Test
    void isPDF() throws IOException {

        //Given
        InputStream inputStream = new ByteArrayInputStream("%PDF-1.4\n%".getBytes());
        MultipartFile testFile = new MockMultipartFile("TestFile", "TestFile.pdf", "application/pdf", inputStream);

        //When
        boolean result = fileUtil.isPDF(testFile);

        //Then
        assertTrue(result);

    }

    @Test
    void isVideo() throws IOException {

        //Given
        InputStream inputStream = new ByteArrayInputStream(new byte[]{0x00, 0x00, 0x00, 0x14, 0x66, 0x74, 0x79, 0x70});
        MultipartFile testFile = new MockMultipartFile("TestFile", "TestFile.mp4", "video/mp4", inputStream);

        //When
        boolean result = fileUtil.isVideo(testFile);

        //Then
        assertTrue(result);

    }

    @Test
    void isVideoMKV() throws IOException {

        //Given
        InputStream inputStream = new ByteArrayInputStream(new byte[]{0x1A, 0x45, (byte) 0xDF, (byte) 0xA3, (byte) 0x93, 0x42, (byte) 0x82, (byte) 0x88});
        MultipartFile testFile = new MockMultipartFile("TestFile", "TestFile.mkv", "application/x-matroska", inputStream);

        //When
        boolean result = fileUtil.isVideo(testFile);

        //Then
        assertTrue(result);

    }

    @Test
    void isVideoIOException() throws IOException {

        //When
        when(mockMultipartFile.getInputStream()).thenThrow(new IOException());
        boolean result = fileUtil.isVideo(mockMultipartFile);

        //Then
        assertFalse(result);

    }

    @Test
    void isPDFIOException() throws IOException {

        //When
        when(mockMultipartFile.getInputStream()).thenThrow(new IOException());
        boolean result = fileUtil.isPDF(mockMultipartFile);

        //Then
        assertFalse(result);

    }

    @Test
    void isImageIOException() throws IOException {

        //When
        when(mockMultipartFile.getInputStream()).thenThrow(new IOException());
        boolean result = fileUtil.isImage(mockMultipartFile);

        //Then
        assertFalse(result);

    }

}
