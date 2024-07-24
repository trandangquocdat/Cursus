package com.fpt.cursus.service.impl;

import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.service.FileService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class FileServiceImpl implements FileService {
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${firebase.storage.bucket}")
    private String bucketName;
    @Value("${fcm.credentials.file.path}")
    private String credentialsFilePath;
    private Storage storage;

    @Autowired
    public FileServiceImpl(SimpMessagingTemplate messagingTemplate
    ) {
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
    public void uploadFile(MultipartFile file, String filename) throws IOException {
        BlobId blobId = BlobId.of(bucketName, filename);
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

    public String getSignedImageUrl(String filename) {
        URL signedUrl = storage.signUrl(
                BlobInfo.newBuilder(BlobId.of(bucketName, filename)).build(),
                15, TimeUnit.MINUTES,
                Storage.SignUrlOption.withV4Signature());
        return signedUrl.toString();
    }

    private void sendProgressUpdate(double progress) {
        // Send progress update to client with sessionId using WebSocket
        messagingTemplate.convertAndSend("/topic/upload-status", String.valueOf(progress));
    }

    @Override
    public String linkSave(MultipartFile file, String folder) {
        String fileName = generateUniqueFileName(folder, file.getOriginalFilename());
        try {
            uploadFile(file, fileName);
        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAIL);
        }
        return fileName;
    }


    private String generateUniqueFileName(String folderName, String originalFileName) {
        String uniqueId = UUID.randomUUID().toString();
        return folderName + "/" + uniqueId + "_" + originalFileName;
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
