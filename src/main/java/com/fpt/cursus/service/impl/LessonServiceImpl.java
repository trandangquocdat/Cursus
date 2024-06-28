package com.fpt.cursus.service.impl;

import com.fpt.cursus.dto.request.CreateLessonDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Chapter;
import com.fpt.cursus.entity.Lesson;
import com.fpt.cursus.enums.status.LessonStatus;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.LessonRepo;
import com.fpt.cursus.service.ChapterService;
import com.fpt.cursus.service.LessonService;
import com.fpt.cursus.util.AccountUtil;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class LessonServiceImpl implements LessonService {
    private final LessonRepo lessonRepo;
    private final ChapterService chapterService;
    private final AccountUtil accountUtil;

    public LessonServiceImpl(LessonRepo lessonRepo,
                             ChapterService chapterService,
                             AccountUtil accountUtil) {
        this.lessonRepo = lessonRepo;
        this.chapterService = chapterService;
        this.accountUtil = accountUtil;
    }

    public Lesson createLesson(Long chapterId, CreateLessonDto request) {
        Chapter chapter = chapterService.findChapterById(chapterId);
        Account account = accountUtil.getCurrentAccount();
        Date date = new Date();
        Lesson lesson = new Lesson();
        lesson.setName(request.getName());
        lesson.setDescription(request.getDescription());
        lesson.setChapter(chapter);
        lesson.setCreatedDate(date);
        lesson.setCreatedBy(account.getUsername());
        return lessonRepo.save(lesson);
    }

    public Lesson findLessonById(Long id) {
        return lessonRepo.findLessonById(id);
    }

    public void deleteLessonById(Long id) {
        Lesson lesson = this.findLessonById(id);
        lesson.setChapter(null);
        lesson.setStatus(LessonStatus.DELETED);
        lessonRepo.save(lesson);
    }

    public void updateLesson(Long id, CreateLessonDto request) {
        Lesson lesson = this.findLessonById(id);
        lesson.setName(request.getName());
        lesson.setDescription(request.getDescription());
        lesson.setUpdatedDate(new Date());
        lesson.setUpdatedBy(accountUtil.getCurrentAccount().getUsername());
        lessonRepo.save(lesson);
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

}
