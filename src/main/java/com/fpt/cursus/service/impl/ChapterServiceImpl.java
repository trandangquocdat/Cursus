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

    public Chapter createChapter(Long courseId, CreateChapterRequest request) {
        Course course = courseService.getCourseById(courseId);
        Account account = accountUtil.getCurrentAccount();
        Date date = new Date();
        Chapter chapter = modelMapper.map(request, Chapter.class);
        chapter.setCreatedDate(date);
        chapter.setCourse(course);
        chapter.setStatus(ChapterStatus.ACTIVE);
        chapter.setCreatedBy(account.getUsername());
        return chapterRepo.save(chapter);
    }

    public Chapter deleteChapterById(Long id) {
        Chapter chapter = this.findChapterById(id);
        if (chapter == null) {
            throw new AppException(ErrorCode.CHAPTER_NOT_FOUND);
        }
        chapter.setStatus(ChapterStatus.DELETED);
        chapter.setCourse(null);
        return chapterRepo.save(chapter);
    }

    public Chapter updateChapter(Long id, UpdateChapterDto request) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true);
        Chapter chapter = this.findChapterById(id);
        mapper.map(request, chapter);
        chapter.setUpdatedDate(new Date());
        chapter.setUpdatedBy(accountUtil.getCurrentAccount().getUsername());
        return chapterRepo.save(chapter);
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
 