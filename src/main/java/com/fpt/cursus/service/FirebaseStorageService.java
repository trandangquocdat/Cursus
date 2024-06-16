package com.fpt.cursus.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ResourceLoader;
import com.google.cloud.storage.Blob;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

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
        // Create a unique filename for the uploaded file
        String fileName = generateUniqueFileName(file.getOriginalFilename());

        // Create BlobId, which has the path of the file in storage
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();

        // Upload file to Firebase Storage
        try (InputStream inputStream = file.getInputStream()) {
            storage.create(blobInfo, inputStream);
            return fileName;
        } catch (StorageException e) {
            throw new IOException("Failed to upload file to Firebase Storage.", e);
        }
    }

    private String generateUniqueFileName(String originalFileName) {
        // Generate a unique filename here, if needed
        return originalFileName;  // You can customize this logic to generate a unique filename
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
