package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.CreateChapterRequest;
import com.fpt.cursus.entity.Chapter;

import java.util.List;

public interface ChapterService {
    Chapter createChapter(Long courseId, CreateChapterRequest request);

    void deleteChapterById(Long id);

    void updateChapter(Long id, CreateChapterRequest request);

    Chapter findChapterById(Long id);

    List<Chapter> findAll();

    List<Chapter> findAllByCourseId(Long id);
}
