package com.fpt.cursus.repository;

import com.fpt.cursus.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterRepo extends JpaRepository<Chapter, Long> {

    Chapter findChapterById(Long id);

    List<Chapter> findAllByCourseId(Long id);

    List<Chapter> findAll();
}
