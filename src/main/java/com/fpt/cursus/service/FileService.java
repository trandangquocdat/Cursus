package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.RegisterReqDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.entity.Lesson;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public interface FileService {
    void uploadFile(MultipartFile file,String folder) throws IOException;
    Resource downloadFileAsResource(String bucketName, String fileName);
    byte[] downloadFileAsBytes(String bucketName, String fileName);
    String getSignedImageUrl(String filename);
    String linkSave(MultipartFile file, String folder);

}

