package com.fpt.cursus.controller;

import com.fpt.cursus.service.FileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@Tag(name = "File Controller")
public class FileUploadController {

    private final FileService storageService;

    @Autowired
    public FileUploadController(FileService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/files/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            // Start the file upload process
            storageService.uploadFile(file, file.getOriginalFilename());
            return "File uploaded successfully: " + file.getOriginalFilename();
        } catch (IOException e) {
            e.printStackTrace(); // Handle exception as needed
            return "File upload failed: " + e.getMessage();
        }
    }

    @GetMapping("/files/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        String bucketName = "cursus-b6cde.appspot.com"; // Replace with your Firebase Storage bucket name
        Resource content = storageService.downloadFileAsResource(bucketName, fileName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);
        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }
}
