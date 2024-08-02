package com.fpt.cursus.repository;

import com.fpt.cursus.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizRepo extends JpaRepository<Quiz, Long> {

    Optional<Quiz> findByName(String name);
}
