package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.CreateLessonDto;
import com.fpt.cursus.entity.Lesson;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface LessonService {
    List<String> uploadLessonFromExcel(Long chapterId, MultipartFile excelFile) throws IOException;

    Lesson createLesson(Long chapterId, CreateLessonDto request);

    Lesson findLessonById(Long id);

    Lesson deleteLessonById(Long id);

    Lesson updateLesson(Long id, CreateLessonDto request);

    List<Lesson> findAllByChapterId(Long id);

    List<Lesson> findAll();

    void save(Lesson lesson);
}
