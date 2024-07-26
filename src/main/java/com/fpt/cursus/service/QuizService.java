package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.CheckAnswerReq;
import com.fpt.cursus.dto.response.QuizRes;
import com.fpt.cursus.dto.response.QuizResultRes;
import com.fpt.cursus.entity.Quiz;
import org.springframework.web.multipart.MultipartFile;

public interface QuizService {
    Quiz createQuiz(MultipartFile excelFile,Long courseId,String name);
    QuizRes getQuizById(Long id);
    QuizResultRes scoringQuiz(CheckAnswerReq request);
    QuizRes getAnswerById(Long id);
}
