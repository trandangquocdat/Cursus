package com.fpt.cursus.service;

import com.fpt.cursus.entity.Lesson;
import com.fpt.cursus.enums.status.LessonStatus;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.util.AccountUtil;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;
import com.google.cloud.storage.Blob;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class FirebaseStorageService {

    private final ChapterService chapterService;
    private final LessonService lessonService;
    private final AccountUtil accountUtil;
    public FirebaseStorageService(ChapterService chapterService,AccountUtil accountUtil,LessonService lessonService) {
        this.chapterService = chapterService;
        this.accountUtil = accountUtil;
        this.lessonService = lessonService;
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
    public List<String> uploadFilesFromExcel(Long chapterId,MultipartFile excelFile) throws IOException {
        List<String> uploadedFileUrls = new ArrayList<>();

        try (InputStream inputStream = excelFile.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                Cell videoLinkCell  = row.getCell(0);
                Cell lessonNameCell = row.getCell(1);
                Cell descriptionCell = row.getCell(2);
                if (videoLinkCell != null && lessonNameCell != null && descriptionCell != null) {
                    String videoLink  = videoLinkCell.getStringCellValue();
                    String lessonName = lessonNameCell.getStringCellValue();
                    String description = descriptionCell.getStringCellValue();
                    Lesson lesson = new Lesson();
                    lesson.setChapter(chapterService.findChapterById(chapterId));
                    lesson.setName(lessonName);
                    lesson.setDescription(description);
                    lesson.setCreatedBy(accountUtil.getCurrentAccount().getUsername());
                    lesson.setCreatedDate(new Date());
                    lesson.setStatus(LessonStatus.ACTIVE);
                    MultipartFile file = getFileFromPath(videoLink);
                    if (file != null) {
                        String fileUrl = uploadFile(file);
                        uploadedFileUrls.add(fileUrl);
                        lesson.setVideoLink(fileUrl);
                    }
                    lessonService.save(lesson);
                }
            }
        } catch (Exception e) {
            throw new IOException("Failed to read Excel file", e);
        }
        return uploadedFileUrls;
    }

    private MultipartFile getFileFromPath(String filePath) {
        try {
            File file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);
            return new MockMultipartFile(file.getName(), file.getName(), "application/octet-stream", fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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
