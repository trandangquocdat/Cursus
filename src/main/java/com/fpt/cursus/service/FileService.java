package com.fpt.cursus.service;

import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.entity.Lesson;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public interface FileService {
    String uploadFile(MultipartFile file) throws IOException;
    Resource downloadFileAsResource(String bucketName, String fileName);
    byte[] downloadFileAsBytes(String bucketName, String fileName);
    void setAvatar(MultipartFile file, Account account);
    void setPicture(MultipartFile file, Course course);
    void setVideo(MultipartFile file, Lesson lesson);
}

