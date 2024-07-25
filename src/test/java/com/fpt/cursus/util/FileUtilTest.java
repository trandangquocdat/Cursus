package com.fpt.cursus.util;

import org.apache.tika.Tika;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {FileUtil.class})
class FileUtilTest {

    @Mock
    private Tika tika;

    @InjectMocks
    private FileUtil fileUtil;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getFileFromPathTest() throws IOException {

        //Given
        File mockFile = File.createTempFile("TestFile", ".txt");
        FileOutputStream out = new FileOutputStream(mockFile);
        out.write("Test Content".getBytes());

        //When
        MultipartFile result = fileUtil.getFileFromPath(mockFile.getPath());

        //Then
        assertEquals(mockFile.getName(), result.getName());
        assertEquals("application/octet-stream", result.getContentType());
        assertEquals("Test Content", new String(result.getBytes()));
        mockFile.delete();
    }

    @Test
    void isImage() throws IOException {

        //Given
        InputStream inputStream = new ByteArrayInputStream(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        MultipartFile mockMultiplepartFile = new MockMultipartFile("TestFile", "TestFile.jpeg", "image/jpeg", inputStream);

        //When
        boolean result = fileUtil.isImage(mockMultiplepartFile);

        //Then
        assertTrue(result);

    }

    @Test
    void isPDF() throws IOException {

        //Given
        InputStream inputStream = new ByteArrayInputStream("%PDF-1.4\n%".getBytes());
        MultipartFile mockMultiplepartFile = new MockMultipartFile("TestFile", "TestFile.pdf", "application/pdf", inputStream);

        //When
        boolean result = fileUtil.isPDF(mockMultiplepartFile);

        //Then
        assertTrue(result);

    }

    @Test
    void isVideo() throws IOException {

        //Given
        InputStream inputStream = new ByteArrayInputStream(new byte[]{0x00, 0x00, 0x00, 0x14, 0x66, 0x74, 0x79, 0x70});
        MultipartFile mockMultiplepartFile = new MockMultipartFile("TestFile", "TestFile.mp4", "video/mp4", inputStream);

        //When
        boolean result = fileUtil.isVideo(mockMultiplepartFile);

        //Then
        assertTrue(result);

    }


}
