package com.fpt.cursus.service;

import com.fpt.cursus.dto.object.QuizQuestion;
import com.fpt.cursus.dto.request.CheckAnswerReq;
import com.fpt.cursus.dto.response.QuizRes;
import com.fpt.cursus.dto.response.QuizResultRes;
import com.fpt.cursus.entity.Quiz;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface QuizService {
    Quiz createQuiz(MultipartFile excelFile, Long courseId, String name);

    QuizRes getQuizByCourseId(Long id);

    QuizResultRes scoringQuiz(CheckAnswerReq request);

    List<QuizQuestion> getAnswerById(Long id);
}
