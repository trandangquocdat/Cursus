package com.fpt.cursus.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.dto.object.QuizAnswer;
import com.fpt.cursus.dto.object.QuizQuestion;
import com.fpt.cursus.dto.object.UserAnswerDto;
import com.fpt.cursus.dto.request.CheckAnswerReq;
import com.fpt.cursus.dto.response.QuizRes;
import com.fpt.cursus.dto.response.QuizResultRes;
import com.fpt.cursus.entity.Quiz;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.QuizRepo;
import com.fpt.cursus.service.CourseService;
import com.fpt.cursus.service.QuizService;
import com.fpt.cursus.util.AccountUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class QuizServiceImpl implements QuizService {
    private static final int QUESTION_ID_INDEX = 0;
    private static final int QUESTION_CONTENT_INDEX = 1;
    private static final int ANSWER_START_INDEX = 2;
    private static final int ANSWER_END_INDEX = 5;
    private static final int CORRECT_ANSWER_INDEX = 6;

    private final QuizRepo quizRepo;
    private final ObjectMapper objectMapper;
    private final AccountUtil accountUtil;
    private final CourseService courseService;

    @Autowired
    public QuizServiceImpl(QuizRepo quizRepo,
                           ObjectMapper objectMapper,
                           AccountUtil accountUtil,
                           CourseService courseService) {
        this.quizRepo = quizRepo;
        this.objectMapper = objectMapper;
        this.accountUtil = accountUtil;
        this.courseService = courseService;
    }

    private static void removeIsCorrectField(List<QuizQuestion> questions) {
        for (QuizQuestion question : questions) {
            List<QuizAnswer> answers = question.getAnswers();
            for (QuizAnswer answer : answers) {
                answer.setIsCorrect(null);
            }
        }
    }

    @Override
    public Quiz createQuiz(MultipartFile excelFile, Long courseId, String name) {
        List<QuizQuestion> quizQuestions = new ArrayList<>();
        Quiz quiz = new Quiz();
        quiz.setName(name);
        quiz.setCreatedDate(new Date());
        quiz.setCreatedBy(accountUtil.getCurrentAccount().getUsername());
        quiz.setCourse(courseService.getCourseById(courseId));
        try (Workbook workbook = new XSSFWorkbook(excelFile.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0); // Assuming the data is in the first sheet
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) {
                rowIterator.next(); // Skip header
            }
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                QuizQuestion quizQuestion = extractQuizQuestion(row);
                quizQuestions.add(quizQuestion);
            }
            assignQuestionScores(quizQuestions);
            saveQuizQuestions(quiz, quizQuestions);
        } catch (IOException e) {
            throw new AppException(ErrorCode.QUIZ_READ_FAIL);
        }
        return quizRepo.save(quiz);
    }

    private QuizQuestion extractQuizQuestion(Row row) {
        QuizQuestion quizQuestion = new QuizQuestion();
        List<QuizAnswer> answers = new ArrayList<>();
        IntStream.range(0, row.getLastCellNum()).forEach(cellIndex -> {
            Cell cell = row.getCell(cellIndex);
            if (cell != null) {
                switch (cellIndex) {
                    case QUESTION_ID_INDEX -> quizQuestion.setQuestionId((int) cell.getNumericCellValue());
                    case QUESTION_CONTENT_INDEX -> quizQuestion.setQuestionContent(cell.toString());
                    case ANSWER_START_INDEX, ANSWER_START_INDEX + 1, ANSWER_START_INDEX + 2, ANSWER_END_INDEX -> {
                        QuizAnswer answer = new QuizAnswer();
                        answer.setContent(cell.toString());
                        answer.setIsCorrect(false);
                        answer.setQuestionId(quizQuestion.getQuestionId());
                        answer.setId(getColumnLetter(cellIndex));
                        answers.add(answer);
                    }
                    case CORRECT_ANSWER_INDEX -> markCorrectAnswer(answers, cell.toString());
                    default -> throw new AppException(ErrorCode.QUIZ_READ_FAIL);
                }
            }
        });
        quizQuestion.setAnswers(answers);
        return quizQuestion;
    }

    private void markCorrectAnswer(List<QuizAnswer> answers, String res) {
        switch (res) {
            case "A" -> answers.get(0).setIsCorrect(true);
            case "B" -> answers.get(1).setIsCorrect(true);
            case "C" -> answers.get(2).setIsCorrect(true);
            case "D" -> answers.get(3).setIsCorrect(true);
            default -> throw new AppException(ErrorCode.QUIZ_READ_FAIL);
        }
    }

    private String getColumnLetter(int cellIndex) {
        return switch (cellIndex) {
            case 2 -> "A";
            case 3 -> "B";
            case 4 -> "C";
            case 5 -> "D";
            default -> throw new AppException(ErrorCode.QUIZ_READ_FAIL);
        };
    }

    private void assignQuestionScores(List<QuizQuestion> quizQuestions) {
        float scorePerQuestion = 10f / quizQuestions.size();
        quizQuestions.forEach(question -> question.setQuestionScore(scorePerQuestion));
    }

    private void saveQuizQuestions(Quiz quiz, List<QuizQuestion> quizQuestions) {
        try {
            quiz.setQuizJson(objectMapper.writeValueAsString(quizQuestions));
            quizRepo.save(quiz);
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.QUIZ_READ_FAIL);
        }
    }

    @Override
    public QuizRes getQuizById(Long id) {
        Quiz quiz = quizRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));
        QuizRes quizRes = new QuizRes();
        quizRes.setQuiz(quiz);
        List<QuizQuestion> questions = getQuizQuestions(quiz);
        removeIsCorrectField(questions);
        quizRes.setQuestions(questions);
        return quizRes;
    }

    @Override
    public List<QuizQuestion> getAnswerById(Long id) {
        Quiz quiz = quizRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));
        return getQuizQuestions(quiz);
    }

    public List<QuizQuestion> getQuizQuestions(Quiz quiz) {
        if (quiz.getQuizJson() == null || quiz.getQuizJson().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(quiz.getQuizJson(), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.QUIZ_READ_FAIL);
        }
    }


    @Override
    public QuizResultRes scoringQuiz(CheckAnswerReq request) {
        int correctCount = 0;
        int wrongCount = 0;
        int skippedCount = 0;
        double totalScore = 0;
        // Ensure no duplicate question IDs
        List<UserAnswerDto> userAnswers = request.getAnswers();
        if (userAnswers == null || userAnswers.isEmpty() || userAnswers.get(0).getAnswerId() == null) {
            QuizResultRes res = new QuizResultRes();
            res.setCorrect(correctCount);
            res.setWrong(wrongCount);
            res.setSkipped(10);
            res.setScore(totalScore);
            return res;
        }
        Set<Integer> questionIds = new HashSet<>();

        for (UserAnswerDto answer : userAnswers) {
            if (!questionIds.add(answer.getQuestionId())) {
                throw new AppException(ErrorCode.DUPLICATE_QUESTION_ID);
            }
        }

        Quiz quiz = quizRepo.findById(request.getQuizId())
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));

        List<QuizQuestion> correctAnswers = getAnswerById(quiz.getId());

        // Create a map for quick lookups of user answers
        Map<Integer, String> userAnswerMap = request.getAnswers().stream()
                .collect(Collectors.toMap(UserAnswerDto::getQuestionId, UserAnswerDto::getAnswerId));

        // Create a map for quick lookups of correct answers
        Map<Integer, String> correctAnswerMap = correctAnswers.stream()
                .collect(Collectors.toMap(
                        QuizQuestion::getQuestionId,
                        question -> question.getAnswers().stream()
                                .filter(QuizAnswer::getIsCorrect)
                                .findFirst()
                                .map(QuizAnswer::getId)
                                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_READ_FAIL))// Ensure that there's always a correct answer
                ));


        QuizResultRes result = new QuizResultRes();



        for (QuizQuestion question : correctAnswers) {
            String userAnswerId = userAnswerMap.get(question.getQuestionId());
            if (userAnswerId == null) {
                skippedCount++;
                continue;
            }

            boolean isCorrect = userAnswerId.equals(correctAnswerMap.get(question.getQuestionId()));
            if (isCorrect) {
                correctCount++;
                totalScore += question.getQuestionScore();
            } else {
                wrongCount++;
            }
        }

        result.setCorrect(correctCount);
        result.setWrong(wrongCount);
        result.setSkipped(skippedCount);
        result.setScore(totalScore);

        return result;
    }


}
