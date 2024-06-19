package com.fpt.cursus.service;

import com.fpt.cursus.entity.Chapter;
import com.fpt.cursus.repository.ChapterRepo;
import com.fpt.cursus.util.AccountUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@MockBeans({
        @MockBean(AccountUtil.class),
        @MockBean(CourseService.class),
        @MockBean(ChapterRepo.class),
})
@ContextConfiguration(classes = {
        ChapterService.class,
        AccountUtil.class,
        CourseService.class,
        ChapterRepo.class,
})
class ChapterServiceTest {
    @Autowired
    private ChapterService chapterService;

    @Autowired
    private AccountUtil accountUtil;

    @Autowired
    private CourseService courseService;

    @Autowired
    private ChapterRepo chapterRepo;

    @BeforeEach
    void initDate() {

    }

    @Test
    void testCreateChapter() {
        Chapter chapter = new Chapter();

    }
}
