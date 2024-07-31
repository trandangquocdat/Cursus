package com.fpt.cursus.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.dto.object.QuizAnswer;
import com.fpt.cursus.dto.object.QuizQuestion;
import com.fpt.cursus.dto.object.UserAnswerDto;
import com.fpt.cursus.dto.request.CheckAnswerReq;
import com.fpt.cursus.dto.response.QuizRes;
import com.fpt.cursus.dto.response.QuizResultRes;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.entity.Quiz;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.repository.QuizRepo;
import com.fpt.cursus.service.impl.QuizServiceImpl;
import com.fpt.cursus.util.AccountUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private QuizAnswer mockAnswer;

    @Mock
    private QuizQuestion mockQuestion;

    @Mock
    private List<QuizQuestion> mockQuestions;

    @Mock
    private Quiz mockQuiz;

    @Mock
    private MultipartFile mockExcelFile;

    @Mock
    private QuizRepo quizRepo;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AccountUtil accountUtil;

    @Mock
    private CourseService courseService;

    @InjectMocks
    private QuizServiceImpl quizService;

    @BeforeEach
    void setUp() throws IOException {
        mockQuiz = new Quiz();
        mockQuiz.setName("TestName");
        mockQuiz.setCreatedDate(new Date());
        mockQuiz.setCreatedBy("TestUser");
        mockQuiz.setCourse(new Course());
        mockQuiz.setQuizJson("[{\"questionId\":1,\"questionContent\":\"TestQuestion\",\"questionScore\":0.25,\"answers\":[{\"id\":\"1\",\"content\":\"TestAnswer\",\"isCorrect\":true}]}]");
        mockQuestion = new QuizQuestion();
        mockQuestion.setQuestionId(1);
        mockQuestion.setQuestionContent("TestQuestion");
        mockQuestion.setQuestionScore(0.25);
        mockAnswer = new QuizAnswer();
        mockAnswer.setId("1");
        mockAnswer.setContent("TestAnswer");
        mockAnswer.setIsCorrect(true);
        mockQuestion.setAnswers(Arrays.asList(mockAnswer));
        mockQuestions = new ArrayList<>();
        mockQuestions.add(mockQuestion);
    }

    @Test
    void createQuiz() throws IOException {

        //Given
        mockExcelFile = mock(MultipartFile.class);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        Row dataRow = sheet.createRow(0);
        Row dataRow2 = sheet.createRow(1);
        dataRow2.createCell(0).setCellValue(1);
        dataRow2.createCell(1).setCellValue("Test");
        dataRow2.createCell(2).setCellValue("A");
        dataRow2.createCell(3).setCellValue("B");
        dataRow2.createCell(4).setCellValue("C");
        dataRow2.createCell(5).setCellValue("D");
        dataRow2.createCell(6).setCellValue("A");
        Row dataRow3 = sheet.createRow(2);
        dataRow3.createCell(0).setCellValue(2);
        dataRow3.createCell(1).setCellValue("Test");
        dataRow3.createCell(2).setCellValue("A");
        dataRow3.createCell(3).setCellValue("B");
        dataRow3.createCell(4).setCellValue("C");
        dataRow3.createCell(5).setCellValue("D");
        dataRow3.createCell(6).setCellValue("B");
        Row dataRow4 = sheet.createRow(3);
        dataRow4.createCell(0).setCellValue(3);
        dataRow4.createCell(1).setCellValue("Test");
        dataRow4.createCell(2).setCellValue("A");
        dataRow4.createCell(3).setCellValue("B");
        dataRow4.createCell(4).setCellValue("C");
        dataRow4.createCell(5).setCellValue("D");
        dataRow4.createCell(6).setCellValue("C");
        Row dataRow5 = sheet.createRow(4);
        dataRow5.createCell(0).setCellValue(4);
        dataRow5.createCell(1).setCellValue("Test");
        dataRow5.createCell(2).setCellValue("A");
        dataRow5.createCell(3).setCellValue("B");
        dataRow5.createCell(4).setCellValue("C");
        dataRow5.createCell(5).setCellValue("D");
        dataRow5.createCell(6).setCellValue("D");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        byte[] workBookToByteArray = baos.toByteArray();
        when(mockExcelFile.getInputStream()).thenReturn(new ByteArrayInputStream(workBookToByteArray));

        //When
        when(accountUtil.getCurrentAccount()).thenReturn(new Account());
        when(courseService.getCourseById(anyLong())).thenReturn(new Course());
        when(quizRepo.save(any(Quiz.class))).thenReturn(mockQuiz);
        Quiz createdQuiz = quizService.createQuiz(mockExcelFile,1l,"TestName");

        //Then
        assertNotNull(createdQuiz);
        assertEquals("TestName", createdQuiz.getName());
        assertEquals("TestUser", createdQuiz.getCreatedBy());

    }

    @Test
    void createQuizAnswerError() throws IOException {

        //Given
        mockExcelFile = mock(MultipartFile.class);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        Row dataRow = sheet.createRow(0);
        Row dataRow2 = sheet.createRow(1);
        dataRow2.createCell(6).setCellValue("E");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        byte[] workBookToByteArray = baos.toByteArray();
        when(mockExcelFile.getInputStream()).thenReturn(new ByteArrayInputStream(workBookToByteArray));

        //When
        when(accountUtil.getCurrentAccount()).thenReturn(new Account());
        when(courseService.getCourseById(anyLong())).thenReturn(new Course());

        //Then
        assertThrows(AppException.class, ()-> quizService.createQuiz(mockExcelFile,1l,"TestName"));

    }

    @Test
    void createQuizCellError() throws IOException {

        //Given
        mockExcelFile = mock(MultipartFile.class);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        Row dataRow = sheet.createRow(0);
        Row dataRow2 = sheet.createRow(1);
        dataRow2.createCell(7).setCellValue("A");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        byte[] workBookToByteArray = baos.toByteArray();
        when(mockExcelFile.getInputStream()).thenReturn(new ByteArrayInputStream(workBookToByteArray));

        //When
        when(accountUtil.getCurrentAccount()).thenReturn(new Account());
        when(courseService.getCourseById(anyLong())).thenReturn(new Course());

        //Then
        assertThrows(AppException.class, ()-> quizService.createQuiz(mockExcelFile,1l,"TestName"));

    }

    @Test
    void createQuizSaveException() throws IOException {

        //Given
        mockExcelFile = mock(MultipartFile.class);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        byte[] workBookToByteArray = baos.toByteArray();
        when(mockExcelFile.getInputStream()).thenReturn(new ByteArrayInputStream(workBookToByteArray));

        //When
        when(accountUtil.getCurrentAccount()).thenReturn(new Account());
        when(courseService.getCourseById(anyLong())).thenReturn(new Course());
        when(objectMapper.writeValueAsString(any(List.class))).thenThrow(JsonProcessingException.class);

        //Then
        assertThrows(AppException.class, ()-> quizService.createQuiz(mockExcelFile,1l,"TestName"));

    }

    @Test
    void createQuizNoData() throws IOException {

        //Given
        mockExcelFile = mock(MultipartFile.class);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        byte[] workBookToByteArray = baos.toByteArray();
        when(mockExcelFile.getInputStream()).thenReturn(new ByteArrayInputStream(workBookToByteArray));

        //When
        when(accountUtil.getCurrentAccount()).thenReturn(new Account());
        when(courseService.getCourseById(anyLong())).thenReturn(new Course());
        when(quizRepo.save(any(Quiz.class))).thenReturn(mockQuiz);
        Quiz createdQuiz = quizService.createQuiz(mockExcelFile,1l,"TestName");

        //Then
        assertNotNull(createdQuiz);

    }

    @Test
    void createQuizFileReadFail() throws IOException {

        //Given
        mockExcelFile = mock(MultipartFile.class);

        //When
        when(accountUtil.getCurrentAccount()).thenReturn(new Account());
        when(courseService.getCourseById(anyLong())).thenReturn(new Course());
        when(mockExcelFile.getInputStream()).thenThrow(new IOException());

        //Then
        assertThrows(AppException.class, () -> quizService.createQuiz(mockExcelFile,1l,"TestName"));

    }

    @Test
    void getQuizByIdTest() throws JsonProcessingException {

        //When
        when(quizRepo.findById(anyLong())).thenReturn(Optional.ofNullable(mockQuiz));
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(mockQuestions);
        QuizRes quizRes = quizService.getQuizById(1L);

        //Then
        assertNotNull(quizRes);
        assertEquals(mockQuiz, quizRes.getQuiz());

    }

    @Test
    void getQuizByIdEmptyJsonTest() throws JsonProcessingException {

        //Given
        mockQuiz.setQuizJson(new String());

        //When
        when(quizRepo.findById(anyLong())).thenReturn(Optional.ofNullable(mockQuiz));
        QuizRes quizRes = quizService.getQuizById(1L);

        //Then
        assertNotNull(quizRes);
        assertEquals(mockQuiz, quizRes.getQuiz());

    }

    @Test
    void getQuizByIdNullJsonTest() throws JsonProcessingException {

        //Given
        mockQuiz.setQuizJson(null);

        //When
        when(quizRepo.findById(anyLong())).thenReturn(Optional.ofNullable(mockQuiz));
        QuizRes quizRes = quizService.getQuizById(1L);

        //Then
        assertNotNull(quizRes);
        assertEquals(mockQuiz, quizRes.getQuiz());

    }

    @Test
    void getQuizByIdReadJsonFailTest() throws JsonProcessingException {

        //When
        when(quizRepo.findById(anyLong())).thenReturn(Optional.ofNullable(mockQuiz));
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenThrow(JsonProcessingException.class);

        //Then
        assertThrows(AppException.class, ()-> quizService.getQuizById(1L));

    }

    @Test
    void getAnswerById() throws JsonProcessingException {

        //When
        when(quizRepo.findById(anyLong())).thenReturn(Optional.ofNullable(mockQuiz));
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(mockQuestions);
        QuizRes quizRes = quizService.getAnswerById(1L);

        //Then
        assertNotNull(quizRes);
        assertEquals(mockQuestions, quizRes.getQuestions());

    }

    @Test
    void scoringQuiz() throws JsonProcessingException {

        //Given
        UserAnswerDto userAnswerDto = new UserAnswerDto();
        userAnswerDto.setAnswerId("1");
        userAnswerDto.setQuestionId(1);
        List<UserAnswerDto> userAnswerDtoList = Arrays.asList(userAnswerDto);
        CheckAnswerReq mockCheckAnswerReq = new CheckAnswerReq();
        mockCheckAnswerReq.setQuizId(1);
        mockCheckAnswerReq.setAnswers(userAnswerDtoList);

        //When
        when(quizRepo.findById(anyLong())).thenReturn(Optional.ofNullable(mockQuiz));
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(mockQuestions);
        QuizResultRes quizResultRes = quizService.scoringQuiz(mockCheckAnswerReq);

        //Then
        assertNotNull(quizResultRes);

    }

    @Test
    void scoringQuizDuplicateAnswer() throws JsonProcessingException {

        //Given
        UserAnswerDto userAnswerDto = new UserAnswerDto();
        userAnswerDto.setAnswerId("1");
        userAnswerDto.setQuestionId(1);
        UserAnswerDto userAnswerDto2 = new UserAnswerDto();
        userAnswerDto2.setAnswerId("1");
        userAnswerDto2.setQuestionId(1);
        List<UserAnswerDto> userAnswerDtoList = Arrays.asList(userAnswerDto,userAnswerDto2);
        CheckAnswerReq mockCheckAnswerReq = new CheckAnswerReq();
        mockCheckAnswerReq.setQuizId(1);
        mockCheckAnswerReq.setAnswers(userAnswerDtoList);

        //When

        //Then
        assertThrows(AppException.class, () -> quizService.scoringQuiz(mockCheckAnswerReq));
    }

    @Test
    void scoringQuizNotCorrect() throws JsonProcessingException {

        //Given
        mockAnswer.setIsCorrect(false);
        UserAnswerDto userAnswerDto = new UserAnswerDto();
        userAnswerDto.setAnswerId("1");
        userAnswerDto.setQuestionId(1);
        List<UserAnswerDto> userAnswerDtoList = Arrays.asList(userAnswerDto);
        CheckAnswerReq mockCheckAnswerReq = new CheckAnswerReq();
        mockCheckAnswerReq.setQuizId(1);
        mockCheckAnswerReq.setAnswers(userAnswerDtoList);

        //When
        when(quizRepo.findById(anyLong())).thenReturn(Optional.ofNullable(mockQuiz));
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(mockQuestions);
        QuizResultRes quizResultRes = quizService.scoringQuiz(mockCheckAnswerReq);

        //Then
        assertNotNull(quizResultRes);

    }

    @Test
    void scoringQuizNoAnswers() throws JsonProcessingException {

        //Given
        UserAnswerDto userAnswerDto = new UserAnswerDto();
        List<UserAnswerDto> userAnswerDtoList = Arrays.asList(userAnswerDto);
        CheckAnswerReq mockCheckAnswerReq = new CheckAnswerReq();
        mockCheckAnswerReq.setQuizId(1);
        mockCheckAnswerReq.setAnswers(userAnswerDtoList);

        //When
        when(quizRepo.findById(anyLong())).thenReturn(Optional.ofNullable(mockQuiz));
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(mockQuestions);
        QuizResultRes quizResultRes = quizService.scoringQuiz(mockCheckAnswerReq);

        //Then
        assertNotNull(quizResultRes);

    }

    @Test
    void scoringQuizWrongUserAnswerId() throws JsonProcessingException {

        //Given
        UserAnswerDto userAnswerDto = new UserAnswerDto();
        userAnswerDto.setAnswerId("0");
        userAnswerDto.setQuestionId(1);
        List<UserAnswerDto> userAnswerDtoList = Arrays.asList(userAnswerDto);
        CheckAnswerReq mockCheckAnswerReq = new CheckAnswerReq();
        mockCheckAnswerReq.setQuizId(1);
        mockCheckAnswerReq.setAnswers(userAnswerDtoList);

        //When
        when(quizRepo.findById(anyLong())).thenReturn(Optional.ofNullable(mockQuiz));
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(mockQuestions);
        QuizResultRes quizResultRes = quizService.scoringQuiz(mockCheckAnswerReq);

        //Then
        assertNotNull(quizResultRes);

    }

}