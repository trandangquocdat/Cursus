package com.fpt.cursus.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {
    void uploadFile(MultipartFile file, String folder) throws IOException;

    Resource downloadFileAsResource(String bucketName, String fileName);

    byte[] downloadFileAsBytes(String bucketName, String fileName);

    String getSignedImageUrl(String filename);

    String linkSave(MultipartFile file, String folder);

}

