package com.fpt.cursus.service;


import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;
import com.google.cloud.storage.Blob;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class FileService {
    private final AccountService accountService;
    private final CourseService courseService;
    public FileService(@Lazy AccountService accountService, @Lazy CourseService courseService) {
        this.accountService = accountService;
        this.courseService = courseService;
    }

    @Value("${firebase.storage.bucket}")
    private String bucketName;

    @Value("${fcm.credentials.file.path}")
    private String credentialsFilePath;
    private Storage storage;


    @PostConstruct
    private void initializeStorage() {
        try {
            storage = StorageOptions.newBuilder().setCredentials(GoogleCredentials.fromStream(
                    new ClassPathResource(credentialsFilePath).getInputStream())).build().getService();
        } catch (IOException e) {
            throw new AppException(ErrorCode.STORAGE_INITIALIZE_FAIL);
        }
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
        try (InputStream inputStream = file.getInputStream()) {
            storage.create(blobInfo, inputStream);
            return generateDownloadUrl(fileName);
        } catch (StorageException e) {
            throw new IOException(e);
        }
    }

    @Async
    public void setAvatar(MultipartFile file, Account account) {
        try {
            String link = uploadFile(file);
            account.setAvatar(link);
        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAIL);
        }
        accountService.saveAccount(account);
    }
    @Async
    public void setPicture(MultipartFile file, Course course) {
        try {
            String link = uploadFile(file);
            course.setPictureLink(link);
        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAIL);
        }
        courseService.saveCourse(course);
    }
    private String generateDownloadUrl(String fileName) {
        return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media", bucketName, fileName);
    }

    private String generateUniqueFileName(String originalFileName) {
        String uniqueId = UUID.randomUUID().toString();
        return uniqueId + "_" + originalFileName;
    }

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

    public byte[] downloadFileAsBytes(String bucketName, String fileName) {
        Blob blob = storage.get(bucketName, fileName);
        if (blob != null) {
            return blob.getContent();
        } else {
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        }
    }

}
