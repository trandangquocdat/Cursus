package com.fpt.cursus.repository;

import com.fpt.cursus.entity.Chapter;
import com.fpt.cursus.entity.Course;
import com.fpt.cursus.entity.Lesson;
import com.fpt.cursus.enums.status.ChapterStatus;
import com.fpt.cursus.enums.status.LessonStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class LessonRepoTest {

    @Autowired
    private LessonRepo lessonRepo;

    @Autowired
    private TestEntityManager testEntityManager;

    private Long lessonId1;
    private Long lessonId2;
    private Long chapterId;

    @BeforeEach
    void setUp() {
        Course course = new Course();
        course.setName("Course 1");
        course.setDescription("Description 1");
        course.setCreatedBy("admin");
        course.setUpdatedBy("admin");
        course.setCreatedDate(new Date());
        course.setUpdatedDate(new Date());
        testEntityManager.persist(course);

        Chapter chapter = new Chapter();
        chapter.setName("Chapter 1");
        chapter.setDescription("Description 1");
        chapter.setStatus(ChapterStatus.ACTIVE);
        chapter.setCreatedBy("admin");
        chapter.setUpdatedBy("admin");
        chapter.setCreatedDate(new Date());
        chapter.setUpdatedDate(new Date());
        chapter.setCourse(course);
        testEntityManager.persist(chapter);
        this.chapterId = chapter.getId();

        Lesson lesson = new Lesson();
        lesson.setName("Lesson 1");
        lesson.setDescription("Description 1");
        lesson.setStatus(LessonStatus.ACTIVE);
        lesson.setVideoLink("https://www.youtube.com/watch?v=123456");
        lesson.setCreatedBy("admin");
        lesson.setUpdatedBy("admin");
        lesson.setCreatedDate(new Date());
        lesson.setUpdatedDate(new Date());
        lesson.setChapter(chapter);
        testEntityManager.persist(lesson);
        this.lessonId1 = lesson.getId();

        Lesson lesson2 = new Lesson();
        lesson2.setName("Lesson 2");
        lesson2.setDescription("Description 2");
        lesson2.setStatus(LessonStatus.ACTIVE);
        lesson2.setVideoLink("https://www.youtube.com/watch?v=123456");
        lesson2.setCreatedBy("admin");
        lesson2.setUpdatedBy("admin");
        lesson2.setCreatedDate(new Date());
        lesson2.setUpdatedDate(new Date());
        lesson2.setChapter(chapter);
        testEntityManager.persist(lesson2);
        this.lessonId2 = lesson2.getId();
    }

    @Test
    void shouldFindLessonById() {
        Lesson lesson = lessonRepo.findLessonById(lessonId1);
        assertThat(lesson).isNotNull();
    }

    @Test
    void shouldNotFindLessonById() {
        Lesson lesson = lessonRepo.findLessonById(lessonId2 + 1);
        assertThat(lesson).isNull();
    }

    @Test
    void shouldFindAllByChapterId() {
        List<Lesson> lessons = lessonRepo.findAllByChapterId(chapterId);
        assertThat(lessons).hasSize(2);
    }

    @Test
    void shouldNotFindAllByChapterId() {
        List<Lesson> lessons = lessonRepo.findAllByChapterId(chapterId + 1);
        assertThat(lessons).isEmpty();
    }

    @Test
    void shouldFindAll() {
        List<Lesson> lessons = lessonRepo.findAll();
        assertThat(lessons).hasSize(2);
    }

}
