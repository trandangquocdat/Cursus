package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.CreateLessonDto;
import com.fpt.cursus.entity.Lesson;

import java.util.List;

public interface LessonService {
    Lesson createLesson(Long chapterId, CreateLessonDto request);

    Lesson findLessonById(Long id);

    void deleteLessonById(Long id);

    void updateLesson(Long id, CreateLessonDto request);

    List<Lesson> findAllByChapterId(Long id);

    List<Lesson> findAll();
}
