package com.fpt.cursus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.dto.request.CreateChapterRequest;
import com.fpt.cursus.dto.request.UpdateChapterDto;
import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.entity.Chapter;
import com.fpt.cursus.enums.ChapterStatus;
import com.fpt.cursus.service.ChapterService;
import com.fpt.cursus.util.ApiResUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ChapterController.class)
@ContextConfiguration(classes = {
        ChapterService.class,
        ApiResUtil.class
})
class ChapterControllerTest {

    @MockBean
    private ChapterService chapterService;

    @MockBean
    private ApiResUtil apiResUtil;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    public ChapterControllerTest(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private static Chapter getChapter(CreateChapterRequest reqDto) {
        Chapter chapter = new Chapter();
        chapter.setName(reqDto.getName());
        chapter.setDescription(reqDto.getDescription());
        chapter.setCreatedDate(new Date());
        chapter.setStatus(ChapterStatus.ACTIVE);
        return chapter;
    }

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new ChapterController(chapterService))
                .alwaysDo(print())
                .build();
    }

    @Test
    void testCreateChapter_Success() throws Exception {
        //given
        CreateChapterRequest reqDto = new CreateChapterRequest();
        reqDto.setName("Chapter 1");
        reqDto.setDescription("Description");

        String json = objectMapper.writeValueAsString(reqDto);

        Chapter chapter = getChapter(reqDto);

        // Mocking ApiRes response
        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setMessage("Create chapter successfully");
        apiRes.setData(chapter);

        //when
        when(chapterService.createChapter(anyLong(), any(CreateChapterRequest.class)))
                .thenReturn(chapter);

        //then
        mockMvc.perform(post("/chapter/create")
                        .param("courseId", "1")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(chapter.getName()))
                .andExpect(jsonPath("$.description").value(chapter.getDescription()));
    }

    @Test
    void testDeleteChapter_Success() throws Exception {
        // Setup
        Chapter deletedChapter = new Chapter();

        // Mock ChapterService
        when(chapterService.deleteChapterById(anyLong())).thenReturn(deletedChapter);

        // Perform the DELETE request
        mockMvc.perform(delete("/chapter/delete")
                        .param("chapterId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify ChapterService method was called with the correct chapterId
        verify(chapterService, times(1)).deleteChapterById(1L);
    }

    @Test
    void testDeleteChapter_OtherException() {
        // Arrange
        Long chapterId = 1L;
        when(chapterService.deleteChapterById(anyLong())).thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        Throwable exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(delete("/chapter/delete")
                    .param("chapterId", chapterId.toString())
                    .contentType(MediaType.APPLICATION_JSON));
        });

        assertInstanceOf(RuntimeException.class, exception.getCause());
        assertEquals("Unexpected error", exception.getCause().getMessage());

        // Verify ChapterService method was called with the correct chapterId
        verify(chapterService, times(1)).deleteChapterById(chapterId);
    }

    @Test
    void testUpdateChapter_Success() throws Exception {
        // given
        UpdateChapterDto reqDto = new UpdateChapterDto();
        reqDto.setName("Updated Chapter");
        reqDto.setDescription("Updated Description");

        String json = objectMapper.writeValueAsString(reqDto);

        // Mocking the chapter returned by findChapterById
        Chapter existingChapter = new Chapter();
        existingChapter.setId(1L);
        existingChapter.setName("Original Chapter");
        existingChapter.setDescription("Original Description");

        when(chapterService.findChapterById(1L)).thenReturn(existingChapter);

        // Mocking the updated chapter after save
        Chapter updatedChapter = new Chapter();
        updatedChapter.setId(1L);
        updatedChapter.setName("Updated Chapter");
        updatedChapter.setDescription("Updated Description");

        when(chapterService.updateChapter(anyLong(), any(UpdateChapterDto.class))).thenReturn(updatedChapter);

        // then
        mockMvc.perform(put("/chapter/update-by-id")
                        .param("chapterId", "1")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Chapter"))
                .andExpect(jsonPath("$.description").value("Updated Description"));
    }

    @Test
    void testFindChapterById_Success() throws Exception {
        // given
        Chapter chapter = new Chapter();
        chapter.setId(1L);
        chapter.setName("Chapter 1");
        chapter.setDescription("Description");

        // Mocking the chapter returned by chapterService.findChapterById
        when(chapterService.findChapterById(1L)).thenReturn(chapter);

        // then
        mockMvc.perform(get("/chapter/get-by-id")
                        .param("chapterId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L)) // Assuming JSON directly returns chapter details
                .andExpect(jsonPath("$.name").value("Chapter 1"))
                .andExpect(jsonPath("$.description").value("Description"));
    }

    @Test
    void testFindAll_Success() throws Exception {
        // given
        Chapter chapter = new Chapter();
        chapter.setId(1L);
        chapter.setName("Chapter 1");
        chapter.setDescription("Description");

        List<Chapter> chapters = Collections.singletonList(chapter);

        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setData(chapters);

        // Mocking the chapterService.findAll() method
        when(chapterService.findAll()).thenReturn(chapters);

        // Mocking ApiRes response
        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
                .thenReturn(apiRes);

        // then
        mockMvc.perform(get("/chapter/get-all")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value(chapter.getName()))
                .andExpect(jsonPath("$.data[0].description").value(chapter.getDescription()));
    }

    @Test
    void testFindAllByCourseId_Success() throws Exception {
        // given
        Chapter chapter = new Chapter();
        chapter.setId(1L);
        chapter.setName("Chapter 1");
        chapter.setDescription("Description");

        List<Chapter> chapters = Collections.singletonList(chapter);

        // Mocking the service method
        when(chapterService.findAllByCourseId(anyLong())).thenReturn(chapters);

        // then
        mockMvc.perform(get("/chapter/get-by-course")
                        .param("courseId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$[0].name").value(chapter.getName()),
                        jsonPath("$[0].description").value(chapter.getDescription()));
    }
}
