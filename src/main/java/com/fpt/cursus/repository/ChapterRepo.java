package com.fpt.cursus.repository;

import com.fpt.cursus.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChapterRepo extends JpaRepository<Chapter, Long> {


}
