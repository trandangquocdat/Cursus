package com.fpt.cursus.controller;

import com.fpt.cursus.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(SpringExtension.class)
@WebMvcTest(FileUploadController.class)
@ContextConfiguration(classes = {
        FileService.class
})
class FileUploadControllerTest {

    @MockBean
    private FileService storageService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new FileUploadController(storageService))
                .alwaysDo(print())
                .build();
    }

    @Test
    void handleFileUploadSuccess() throws Exception {
        //given
        MockMultipartFile file = new MockMultipartFile("file",
                "filename",
                MediaType.TEXT_PLAIN_VALUE,
                "content".getBytes());
        //then
        mockMvc.perform(multipart("/files/upload")
                        .file(file))
                .andExpectAll(status().isOk(),
                        content().string("File uploaded successfully: filename"));
    }

    @Test
    void handleFileUploadFailed() throws Exception {
        //given
        MockMultipartFile file = new MockMultipartFile("file",
                "filename",
                MediaType.TEXT_PLAIN_VALUE,
                "content".getBytes());
        //when
        doThrow(new IOException("File upload failed"))
                .when(storageService).uploadFile(file, file.getOriginalFilename());
        //then
        mockMvc.perform(multipart("/files/upload")
                        .file(file))
                .andExpectAll(status().isOk(),
                        content().string("File upload failed: File upload failed"));
    }

    @Test
    void downloadFileSuccess() throws Exception {
        //given
        String filename = "filename";
        byte[] content = "content".getBytes();
        Resource resource = new ByteArrayResource(content);
        //when
        when(storageService.downloadFileAsResource(anyString(), anyString()))
                .thenReturn(resource);
        //then
        mockMvc.perform(get("/files/download/{filename}", filename))
                .andExpectAll(status().isOk(),
                        content().bytes(content),
                        header().string(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"attachment\"; filename=\"" + filename + "\""),
                        header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE));
    }
}
