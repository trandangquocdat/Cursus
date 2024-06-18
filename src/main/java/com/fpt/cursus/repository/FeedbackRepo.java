package com.fpt.cursus.repository;

import com.fpt.cursus.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepo extends JpaRepository<Feedback, Long> {

    Feedback findFeedbackById(Long id);

    List<Feedback> findFeedbackByCourseId(Long id);
}
