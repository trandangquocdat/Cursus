package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.CreateLessonDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Chapter;
import com.fpt.cursus.entity.Lesson;
import com.fpt.cursus.enums.LessonStatus;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.LessonRepo;
import com.fpt.cursus.util.AccountUtil;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class LessonService {
    private final LessonRepo lessonRepo;
    private final ChapterService chapterService;
    private final AccountUtil accountUtil;
    private final ModelMapper modelMapper;
    public LessonService(LessonRepo lessonRepo, @Lazy ChapterService chapterService,
                         AccountUtil accountUtil,ModelMapper modelMapper) {
        this.lessonRepo = lessonRepo;
        this.chapterService = chapterService;
        this.accountUtil = accountUtil;
        this.modelMapper = modelMapper;
    }

    public Lesson createLesson(Long chapterId, CreateLessonDto request) {
        Chapter chapter = chapterService.findChapterById(chapterId);
        Account account = accountUtil.getCurrentAccount();
        Date date = new Date();
        Lesson lesson = modelMapper.map(request, Lesson.class);
        lesson.setChapter(chapter);
        lesson.setCreatedDate(date);
        lesson.setCreatedBy(account.getUsername());
        lesson.setVideoLink(request.getVideoLink());
        return lessonRepo.save(lesson);
    }

    public Lesson findLessonById(Long id) {
        return lessonRepo.findLessonById(id);
    }

    public Lesson deleteLessonById(Long id) {
        Lesson lesson = this.findLessonById(id);
        lesson.setChapter(null);
        lesson.setStatus(LessonStatus.DELETED);
        return lessonRepo.save(lesson);
    }

    public Lesson updateLesson(Long id, CreateLessonDto request) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true);
        Lesson lesson = this.findLessonById(id);
        mapper.map(request, lesson);
        lesson.setUpdatedDate(new Date());
        lesson.setUpdatedBy(accountUtil.getCurrentAccount().getUsername());
        return lessonRepo.save(lesson);
    }

    public List<Lesson> findAllByChapterId(Long id) {
        List<Lesson> lessons = lessonRepo.findAllByChapterId(id);
        if (lessons == null) {
            throw new AppException(ErrorCode.LESSON_NOT_FOUND);
        }
        return lessons;
    }
    public List<Lesson> findAll() {
        return lessonRepo.findAll();
    }
    public void save(Lesson lesson){
        lessonRepo.save(lesson);
    }
}
