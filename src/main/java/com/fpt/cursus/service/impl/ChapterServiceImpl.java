package com.fpt.cursus.service.impl;

import com.fpt.cursus.dto.request.CreateChapterRequest;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Chapter;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.enums.status.ChapterStatus;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.ChapterRepo;
import com.fpt.cursus.service.ChapterService;
import com.fpt.cursus.service.CourseService;
import com.fpt.cursus.util.AccountUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ChapterServiceImpl implements ChapterService {
    private final ChapterRepo chapterRepo;
    private final CourseService courseService;
    private final AccountUtil accountUtil;

    @Autowired
    public ChapterServiceImpl(ChapterRepo chapterRepo,
                              CourseService courseService,
                              AccountUtil accountUtil) {
        this.chapterRepo = chapterRepo;
        this.courseService = courseService;
        this.accountUtil = accountUtil;
    }

    public Chapter createChapter(Long courseId, CreateChapterRequest request) {
        Course course = courseService.getCourseById(courseId);
        Account account = accountUtil.getCurrentAccount();
        Date date = new Date();
        Chapter chapter = new Chapter();
        chapter.setName(request.getName());
        chapter.setDescription(request.getDescription());
        chapter.setCreatedDate(date);
        chapter.setCourse(course);
        chapter.setStatus(ChapterStatus.ACTIVE);
        chapter.setCreatedBy(account.getUsername());
        return chapterRepo.save(chapter);
    }

    public void deleteChapterById(Long id) {
        Chapter chapter = this.findChapterById(id);
        if (chapter == null) {
            throw new AppException(ErrorCode.CHAPTER_NOT_FOUND);
        }
        chapter.setStatus(ChapterStatus.DELETED);
        chapter.setCourse(null);
        chapterRepo.save(chapter);
    }

    public void updateChapter(Long id, CreateChapterRequest request) {
        Chapter chapter = this.findChapterById(id);
        chapter.setName(request.getName());
        chapter.setDescription(request.getDescription());
        chapter.setUpdatedDate(new Date());
        chapter.setUpdatedBy(accountUtil.getCurrentAccount().getUsername());
        chapterRepo.save(chapter);
    }

    public Chapter findChapterById(Long id) {
        Chapter chapter = chapterRepo.findChapterById(id);
        if (chapter == null) {
            throw new AppException(ErrorCode.CHAPTER_NOT_FOUND);
        }
        return chapter;
    }

    public List<Chapter> findAll() {
        return chapterRepo.findAll();
    }

    public List<Chapter> findAllByCourseId(Long id) {
        List<Chapter> chapters = chapterRepo.findAllByCourseId(id);
        if (chapters == null) {
            throw new AppException(ErrorCode.CHAPTER_NOT_FOUND);
        }
        return chapters;
    }
}
