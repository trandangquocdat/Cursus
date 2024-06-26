package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.CreateChapterRequest;
import com.fpt.cursus.entity.Chapter;

import java.util.List;

public interface ChapterService {

    public Chapter createChapter(Long courseId, CreateChapterRequest request);

    public void deleteChapterById(Long id);

    public void updateChapter(Long id, CreateChapterRequest request);

    public Chapter findChapterById(Long id);

    public List<Chapter> findAll();

    public List<Chapter> findAllByCourseId(Long id);

}
