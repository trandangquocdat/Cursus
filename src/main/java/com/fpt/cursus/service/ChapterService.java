package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.CreateChapterRequest;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Chapter;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.repository.ChapterRepo;
import com.fpt.cursus.repository.CourseRepo;
import com.fpt.cursus.util.AccountUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ChapterService {
    @Autowired
    private ChapterRepo chapterRepo;
    @Autowired
    private CourseRepo courseRepo;
    @Autowired
    private AccountUtil accountUtil;
    public Chapter createChapter(CreateChapterRequest request) {
        Account account = accountUtil.getCurrentAccount();
        Date date = new Date();
        Course course = courseRepo.findCourseById(request.getCourseId());
        Chapter chapter = new Chapter();
        chapter.setName(request.getName());
        chapter.setDescription(request.getDescription());
        chapter.setCreatedDate(date);
        chapter.setCourse(course);
        chapter.setCreatedBy(account.getUsername());
        return chapterRepo.save(chapter);
    }
}
