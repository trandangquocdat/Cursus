package com.fpt.cursus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new FileUploadController(storageService))
                .alwaysDo(print())
                .build();
    }

    @Test
    void uploadFileSuccess() throws Exception {
        //given
        String res = "response";
        String content = "content";
        MockMultipartFile multipartFile = new MockMultipartFile("file",
                res, "text/plain",
                content.getBytes());
        //when
        when(storageService.uploadFile(any(MultipartFile.class)))
                .thenReturn(res);
        //then
        mockMvc.perform(multipart("/files/upload")
                        .file(multipartFile))
                .andExpectAll(status().isOk(),
                        content().string(res));
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
