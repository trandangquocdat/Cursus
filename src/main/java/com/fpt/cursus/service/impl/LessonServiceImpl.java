package com.fpt.cursus.service.impl;

import com.fpt.cursus.dto.request.CreateLessonDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Chapter;
import com.fpt.cursus.entity.Lesson;
import com.fpt.cursus.enums.LessonStatus;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.LessonRepo;
import com.fpt.cursus.service.ChapterService;
import com.fpt.cursus.service.FileService;
import com.fpt.cursus.service.LessonService;
import com.fpt.cursus.util.AccountUtil;
import com.fpt.cursus.util.FileUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class LessonServiceImpl implements LessonService {
    private final LessonRepo lessonRepo;
    private final ChapterService chapterService;
    private final AccountUtil accountUtil;
    private final ModelMapper modelMapper;
    private final FileService fileService;
    private final FileUtil fileUtil;

    @Autowired
    public LessonServiceImpl(LessonRepo lessonRepo,
                             @Lazy ChapterService chapterService,
                             AccountUtil accountUtil,
                             ModelMapper modelMapper,
                             FileService fileService,
                             FileUtil fileUtil) {
        this.lessonRepo = lessonRepo;
        this.chapterService = chapterService;
        this.accountUtil = accountUtil;
        this.modelMapper = modelMapper;
        this.fileService = fileService;
        this.fileUtil = fileUtil;
    }

    @Override
    public List<String> uploadLessonFromExcel(Long chapterId, MultipartFile excelFile) throws IOException {
        List<String> uploadedFileUrls = new ArrayList<>();
        String folder = accountUtil.getCurrentAccount().getUsername();
        try (InputStream inputStream = excelFile.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                Cell videoLinkCell = row.getCell(0);
                Cell lessonNameCell = row.getCell(1);
                Cell descriptionCell = row.getCell(2);
                if (videoLinkCell != null && lessonNameCell != null && descriptionCell != null) {
                    String videoLink = videoLinkCell.getStringCellValue();
                    String lessonName = lessonNameCell.getStringCellValue();
                    String description = descriptionCell.getStringCellValue();
                    Lesson lesson = new Lesson();
                    lesson.setChapter(chapterService.findChapterById(chapterId));
                    lesson.setName(lessonName);
                    lesson.setDescription(description);
                    lesson.setCreatedBy(accountUtil.getCurrentAccount().getUsername());
                    lesson.setCreatedDate(new Date());
                    lesson.setStatus(LessonStatus.ACTIVE);
                    MultipartFile file = getFileFromPath(videoLink);
                    if (file != null && fileUtil.isVideo(file)) {
                        String link = fileService.linkSave(file, folder);
                        lesson.setVideoLink(link);
                    }
                    lessonRepo.save(lesson);
                }
            }
        } catch (Exception e) {
            throw new IOException("Failed to read Excel file", e);
        }
        return uploadedFileUrls;
    }

    private MultipartFile getFileFromPath(String filePath) {
        try {
            File file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);
            return new MockMultipartFile(file.getName(), file.getName(), "application/octet-stream", fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Lesson createLesson(Long chapterId, CreateLessonDto request) {
        Chapter chapter = chapterService.findChapterById(chapterId);
        Account account = accountUtil.getCurrentAccount();
        String folder = account.getUsername();
        Date date = new Date();
        Lesson lesson = modelMapper.map(request, Lesson.class);
        lesson.setChapter(chapter);
        lesson.setCreatedDate(date);
        lesson.setCreatedBy(account.getUsername());
        lesson.setStatus(LessonStatus.ACTIVE);
        if (request.getVideoLink() == null) {
            lesson.setVideoLink(null);
        } else if (fileUtil.isVideo(request.getVideoLink())) {
            String link = fileService.linkSave(request.getVideoLink(), folder);
            lesson.setVideoLink(link);
        } else {
            throw new AppException(ErrorCode.FILE_INVALID_VIDEO);
        }
        return lessonRepo.save(lesson);
    }

    @Override

    public Lesson findLessonById(Long id) {
        Lesson lesson = lessonRepo.findLessonById(id);
        lesson.setVideoLink(fileService.getSignedImageUrl(lesson.getVideoLink()));
        return lesson;
    }

    @Override

    public Lesson deleteLessonById(Long id) {
        Lesson lesson = this.findLessonById(id);
        lesson.setChapter(null);
        lesson.setStatus(LessonStatus.DELETED);
        return lessonRepo.save(lesson);
    }

    @Override

    public Lesson updateLesson(Long id, CreateLessonDto request) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true);
        Lesson lesson = this.findLessonById(id);
        String folder = accountUtil.getCurrentAccount().getUsername();
        mapper.map(request, lesson);
        lesson.setUpdatedDate(new Date());
        if (request.getVideoLink() != null) {
            if (fileUtil.isVideo(request.getVideoLink())) {
                String link = fileService.linkSave(request.getVideoLink(), folder);
                lesson.setVideoLink(link);
            } else {
                throw new AppException(ErrorCode.FILE_INVALID_VIDEO);
            }
        }
        lesson.setUpdatedBy(accountUtil.getCurrentAccount().getUsername());
        return lessonRepo.save(lesson);
    }

    @Override

    public List<Lesson> findAllByChapterId(Long id) {
        List<Lesson> lessons = lessonRepo.findAllByChapterId(id);
        if (lessons == null) {
            throw new AppException(ErrorCode.LESSON_NOT_FOUND);
        } else {
            for (Lesson lesson : lessons) {
                lesson.setVideoLink(fileService.getSignedImageUrl(lesson.getVideoLink()));
            }
        }
        return lessons;
    }

    @Override

    public List<Lesson> findAll() {
        List<Lesson> lessons = lessonRepo.findAll();
        for (Lesson lesson : lessons) {
            lesson.setVideoLink(fileService.getSignedImageUrl(lesson.getVideoLink()));
        }
        return lessons;
    }

    @Override

    public void save(Lesson lesson) {
        lessonRepo.save(lesson);
    }
}
