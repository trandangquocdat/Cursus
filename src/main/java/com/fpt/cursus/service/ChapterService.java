package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.CreateChapterRequest;
import com.fpt.cursus.dto.request.UpdateChapterDto;
import com.fpt.cursus.entity.Chapter;

import java.util.List;

public interface ChapterService {
    Chapter createChapter(Long courseId, CreateChapterRequest request);

    Chapter deleteChapterById(Long id);

    Chapter updateChapter(Long id, UpdateChapterDto request);

    Chapter findChapterById(Long id);

    List<Chapter> findAll();

    List<Chapter> findAllByCourseId(Long id);
}
