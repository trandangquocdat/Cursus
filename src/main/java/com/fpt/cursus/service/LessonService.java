package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.CreateLessonDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Chapter;
import com.fpt.cursus.entity.Lesson;
import com.fpt.cursus.enums.status.LessonStatus;
import com.fpt.cursus.repository.LessonRepo;
import com.fpt.cursus.util.AccountUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class LessonService {
    @Autowired
    private LessonRepo lessonRepo;
    @Autowired
    private ChapterService chapterService;
    @Autowired
    private AccountUtil accountUtil;

    public Lesson createLesson(CreateLessonDto request) {
        Chapter chapter = chapterService.findChapterById(request.getChapterId());
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

    public Lesson updateLesson(Long id, CreateLessonDto request) {
        Lesson lesson = this.findLessonById(id);
        lesson.setName(request.getName());
        lesson.setDescription(request.getDescription());
        lesson.setUpdatedDate(new Date());
        lesson.setUpdatedBy(accountUtil.getCurrentAccount().getUsername());
        return lessonRepo.save(lesson);
    }


}
