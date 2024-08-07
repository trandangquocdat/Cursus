package com.fpt.cursus.repository;

import com.fpt.cursus.entity.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizResultRepo extends JpaRepository<QuizResult, Long> {

}
