package com.fpt.cursus.repository;

import com.fpt.cursus.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepo extends JpaRepository<Lesson, Long> {

    Lesson findLessonById(Long id);
}
