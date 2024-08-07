package com.fpt.cursus.util;

import org.apache.tika.Tika;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Component
public class FileUtil {

    private static final Tika tika = new Tika();

    public MultipartFile getFileFromPath(String filePath) {
        try {
            File file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);
            return new MockMultipartFile(file.getName(), file.getName(), "application/octet-stream", fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isImage(MultipartFile file) {
        try {
            // Xác định loại file dựa trên nội dung của file
            String contentType = tika.detect(file.getInputStream());
            // Kiểm tra contentType có phải là ảnh hay không
            return contentType.startsWith("image/");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isPDF(MultipartFile file) {
        try {
            // Xác định loại file dựa trên nội dung của file
            String contentType = tika.detect(file.getInputStream());

            // Kiểm tra contentType có phải là PDF hay không
            return contentType.equals("application/pdf");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isVideo(MultipartFile file) {
        try {
            // Xác định loại file dựa trên nội dung của file
            String contentType = tika.detect(file.getInputStream());

            // Kiểm tra contentType có phải là video hay không
            return contentType.startsWith("video/") || contentType.equals("application/x-matroska");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isExcel(MultipartFile file) {
        Tika tika = new Tika();
        try {
            // Xác định loại file dựa trên nội dung của file
            String contentType = tika.detect(file.getInputStream());

            // Kiểm tra contentType có phải là Excel hay không
            if (contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                    contentType.equals("application/vnd.ms-excel")) {
                return true;
            }

            // Nếu không xác định được bằng contentType, kiểm tra bằng tên file
            String fileName = file.getOriginalFilename();
            return fileName != null && (fileName.endsWith(".xlsx") || fileName.endsWith(".xls"));
        } catch (IOException e) {
            return false;
        }
    }
}