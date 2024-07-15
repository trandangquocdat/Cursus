package com.fpt.cursus.service.impl;

import com.fpt.cursus.dto.request.CreateChapterRequest;
import com.fpt.cursus.dto.request.UpdateChapterDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Chapter;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.enums.ChapterStatus;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.ChapterRepo;
import com.fpt.cursus.service.ChapterService;
import com.fpt.cursus.service.CourseService;
import com.fpt.cursus.util.AccountUtil;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ChapterServiceImpl implements ChapterService {

    private final ChapterRepo chapterRepo;
    private final CourseService courseService;
    private final AccountUtil accountUtil;
    private final ModelMapper modelMapper;

    @Autowired
    public ChapterServiceImpl(@Lazy CourseService courseService,
                              ChapterRepo chapterRepo,
                              AccountUtil accountUtil,
                              ModelMapper modelMapper) {
        this.courseService = courseService;
        this.chapterRepo = chapterRepo;
        this.accountUtil = accountUtil;
        this.modelMapper = modelMapper;
    }

    @Override
    public Chapter createChapter(Long courseId, CreateChapterRequest request) {
        Course course = courseService.getCourseById(courseId);
        Account account = accountUtil.getCurrentAccount();
        Chapter chapter = modelMapper.map(request, Chapter.class);
        chapter.setCreatedDate(new Date());
        chapter.setCourse(course);
        chapter.setStatus(ChapterStatus.ACTIVE);
        chapter.setCreatedBy(account.getUsername());
        return chapterRepo.save(chapter);
    }

    @Override
    public Chapter deleteChapterById(Long id) {
        Chapter chapter = findChapterById(id);
        chapter.setStatus(ChapterStatus.DELETED);
        chapter.setCourse(null);
        return chapterRepo.save(chapter);
    }

    @Override
    public Chapter updateChapter(Long id, UpdateChapterDto request) {
        Chapter chapter = findChapterById(id);
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true);
        modelMapper.map(request, chapter);
        chapter.setUpdatedDate(new Date());
        chapter.setUpdatedBy(accountUtil.getCurrentAccount().getUsername());
        return chapterRepo.save(chapter);
    }

    @Override
    public Chapter findChapterById(Long id) {
        return chapterRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.CHAPTER_NOT_FOUND));
    }

    @Override
    public List<Chapter> findAll() {
        return chapterRepo.findAll();
    }

    @Override
    public List<Chapter> findAllByCourseId(Long courseId) {
        List<Chapter> chapters = chapterRepo.findAllByCourseId(courseId);
        if (chapters.isEmpty()) {
            throw new AppException(ErrorCode.CHAPTER_NOT_FOUND);
        }
        return chapters;
    }
}
