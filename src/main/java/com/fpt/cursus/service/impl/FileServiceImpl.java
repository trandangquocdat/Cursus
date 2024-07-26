package com.fpt.cursus.service.impl;


import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.entity.Lesson;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.service.AccountService;
import com.fpt.cursus.service.CourseService;
import com.fpt.cursus.service.FileService;
import com.fpt.cursus.service.LessonService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

@Service
@Slf4j
public class FileServiceImpl implements FileService {
    private final AccountService accountService;
    private final CourseService courseService;
    private final LessonService lessonService;
    private SimpMessagingTemplate messagingTemplate;

    @Value("${firebase.storage.bucket}")
    private String bucketName;
    @Value("${fcm.credentials.file.path}")
    private String credentialsFilePath;
    private Storage storage;

    @Autowired
    public FileServiceImpl(@Lazy AccountService accountService,
                           @Lazy CourseService courseService,
                           @Lazy LessonService lessonService,
                           SimpMessagingTemplate messagingTemplate
    ) {
        this.accountService = accountService;
        this.courseService = courseService;
        this.lessonService = lessonService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostConstruct
    private void initializeStorage() {
        try {
            storage = StorageOptions.newBuilder().setCredentials(GoogleCredentials.fromStream(
                    new ClassPathResource(credentialsFilePath).getInputStream())).build().getService();
        } catch (IOException e) {
            throw new AppException(ErrorCode.STORAGE_INITIALIZE_FAIL);
        }
    }

    @Override
    public void uploadFile(MultipartFile file) throws IOException {
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();

        try (WriteChannel writer = storage.writer(blobInfo)) {
            byte[] buffer = new byte[10 * 1024 * 1024]; // 10 MB buffer
            int limit;
            InputStream inputStream = file.getInputStream();
            long totalBytes = file.getSize();
            long uploadedBytes = 0;

            while ((limit = inputStream.read(buffer)) >= 0) {
                writer.write(ByteBuffer.wrap(buffer, 0, limit));
                uploadedBytes += limit;
                double progress = (double) uploadedBytes / totalBytes * 100;
                // Send progress update over WebSocket
                log.info("Progress: " + progress);
                sendProgressUpdate(progress);
            }
        } catch (StorageException e) {
            throw new IOException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendProgressUpdate(double progress) {
        // Send progress update to client with sessionId using WebSocket
        messagingTemplate.convertAndSend( "/topic/upload-status", String.valueOf(progress));
    }

    @Override
    public void setAvatar(MultipartFile file, Account account) {
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        String link = generateDownloadUrl(fileName);
        account.setAvatar(link);
        accountService.saveAccount(account);
    }

    @Override
    public void setPicture(MultipartFile file, Course course) {
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        String link = generateDownloadUrl(fileName);
        course.setPictureLink(link);
        courseService.saveCourse(course);
    }

    @Override
    public boolean setVideo(MultipartFile file, Lesson lesson) {
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        String link = generateDownloadUrl(fileName);
        lesson.setVideoLink(link);
        lessonService.save(lesson);
        return true;
    }


    private String generateDownloadUrl(String fileName) {
        return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media", bucketName, fileName);
    }

    private String generateUniqueFileName(String originalFileName) {
        String uniqueId = UUID.randomUUID().toString();
        return uniqueId + "_" + originalFileName;
    }

    @Override
    public Resource downloadFileAsResource(String bucketName, String fileName) {
        Blob blob = storage.get(bucketName, fileName);
        if (blob != null) {
            byte[] content = blob.getContent();
            // Create a ByteArrayResource to wrap the byte[] content
            return new ByteArrayResource(content);
        } else {
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    @Override
    public byte[] downloadFileAsBytes(String bucketName, String fileName) {
        Blob blob = storage.get(bucketName, fileName);
        if (blob != null) {
            return blob.getContent();
        } else {
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        }
    }

}
