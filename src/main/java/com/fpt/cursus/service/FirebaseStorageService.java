package com.fpt.cursus.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.google.cloud.storage.Blob;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class FirebaseStorageService {

    @Value("${firebase.storage.bucket}")
    private String bucketName;

    @Value("${fcm.credentials.file.path}")
    private String credentialsFilePath;
    private Storage storage;


    @PostConstruct
    private void initializeStorage() throws IOException {
        storage = StorageOptions.newBuilder().setCredentials(GoogleCredentials.fromStream(
                new ClassPathResource(credentialsFilePath).getInputStream())).build().getService();
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
        try (InputStream inputStream = file.getInputStream()) {
            storage.create(blobInfo, inputStream);
            return generateDownloadUrl(fileName);
        } catch (StorageException e) {
            throw new IOException("Failed to upload file to Firebase Storage.", e);
        }
    }
    private String generateDownloadUrl(String fileName) {
        return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media", bucketName, fileName);
    }
    private String generateUniqueFileName(String originalFileName) {
        String uniqueId = UUID.randomUUID().toString();
        return uniqueId + "_" + originalFileName;
    }

    public Resource downloadFileAsResource(String bucketName, String fileName) throws IOException {
        Blob blob = storage.get(bucketName, fileName);
        if (blob != null) {
            byte[] content = blob.getContent();
            // Create a ByteArrayResource to wrap the byte[] content
            return new ByteArrayResource(content);
        } else {
            throw new IOException("File not found: " + fileName);
        }
    }
    public byte[] downloadFileAsBytes(String bucketName, String fileName) throws IOException {
        Blob blob = storage.get(bucketName, fileName);
        if (blob != null) {
            return blob.getContent();
        } else {
            throw new IOException("File not found: " + fileName);
        }
    }

}
