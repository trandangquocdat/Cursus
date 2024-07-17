//package com.fpt.cursus.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fpt.cursus.dto.request.CreateChapterRequest;
//import com.fpt.cursus.dto.request.UpdateChapterDto;
//import com.fpt.cursus.dto.response.ApiRes;
//import com.fpt.cursus.entity.Chapter;
//import com.fpt.cursus.enums.ChapterStatus;
//import com.fpt.cursus.service.ChapterService;
//import com.fpt.cursus.service.CourseService;
//import com.fpt.cursus.util.AccountUtil;
//import com.fpt.cursus.util.ApiResUtil;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Collections;
//import java.util.Date;
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
//
//@ExtendWith(SpringExtension.class)
//@WebMvcTest(ChapterController.class)
//@ContextConfiguration(classes = {
//        ChapterService.class,
//        CourseService.class,
//        AccountUtil.class,
//        ApiResUtil.class
//})
//class ChapterControllerTest {
//
//    @MockBean
//    private ChapterService chapterService;
//
//    @MockBean
//    private ApiResUtil apiResUtil;
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @BeforeEach
//    void setUp() {
//        mockMvc = standaloneSetup(new ChapterController(chapterService))
//                .alwaysDo(print())
//                .build();
//    }
//
//    @Test
//    void testCreateChapter_Success() throws Exception {
//        //given
//        CreateChapterRequest reqDto = new CreateChapterRequest();
//        reqDto.setName("Chapter 1");
//        reqDto.setDescription("Description");
//
//        String json = objectMapper.writeValueAsString(reqDto);
//
//        Chapter chapter = getChapter(reqDto);
//
//        ApiRes<Object> apiRes = new ApiRes<>();
//        apiRes.setMessage("Create chapter successfully");
//        apiRes.setData(chapter);
//        //when
//        when(chapterService.createChapter(anyLong(), any(CreateChapterRequest.class)))
//                .thenReturn(chapter);
//        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
//                .thenReturn(apiRes);
//        //then
//        mockMvc.perform(post("/chapter/create")
//                        .param("courseId", "1")
//                        .accept(MediaType.APPLICATION_JSON)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpectAll(status().isOk(),
//                        jsonPath("$.message").value("Create chapter successfully"),
//                        jsonPath("$.data.name").value(chapter.getName()),
//                        jsonPath("$.data.description").value(chapter.getDescription()));
//    }
//
//    @Test
//    void testDeleteChapter_Success() throws Exception {
//        //given
//        String successMessage = "Delete chapter successfully";
//        ApiRes<Object> apiRes = new ApiRes<>();
//        apiRes.setMessage(successMessage);
//        //when
//        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
//                .thenReturn(apiRes);
//        doNothing().when(chapterService).deleteChapterById(anyLong());
//        //then
//        mockMvc.perform(delete("/chapter/delete")
//                        .param("chapterId", "1")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpectAll(status().isOk(),
//                        jsonPath("$.message").value(successMessage));
//    }
//
//    @Test
//    void testUpdateChapter_Success() throws Exception {
//        //given
//        CreateChapterRequest reqDto = new CreateChapterRequest();
//        reqDto.setName("Updated Chapter");
//        reqDto.setDescription("Updated Description");
//
//        String json = objectMapper.writeValueAsString(reqDto);
//
//        String successMessage = "Update chapter successfully";
//        ApiRes<Object> apiRes = new ApiRes<>();
//        apiRes.setMessage(successMessage);
//        //when
//        doNothing().when(chapterService).updateChapter(anyLong(), any(UpdateChapterDto.class));
//        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
//                .thenReturn(apiRes);
//        //then
//        mockMvc.perform(put("/chapter/update/{chapterId}", 1L)
//                        .accept(MediaType.APPLICATION_JSON)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpectAll(status().isOk(),
//                        jsonPath("$.message").value(successMessage));
//    }
//
//    @Test
//    void testFindChapterById_Success() throws Exception {
//        //given
//        Chapter chapter = new Chapter();
//        chapter.setId(1L);
//        chapter.setName("Chapter 1");
//        chapter.setDescription("Description");
//
//        ApiRes<Object> apiRes = new ApiRes<>();
//        apiRes.setData(chapter);
//        //when
//        when(chapterService.findChapterById(anyLong()))
//                .thenReturn(chapter);
//        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
//                .thenReturn(apiRes);
//        //then
//        mockMvc.perform(get("/chapter/get-by-id/{chapterId}", 1L)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpectAll(status().isOk(),
//                        jsonPath("$.data.name").value(chapter.getName()),
//                        jsonPath("$.data.description").value(chapter.getDescription()));
//    }
//
//    @Test
//    void testFindAll_Success() throws Exception {
//        //given
//        Chapter chapter = new Chapter();
//        chapter.setId(1L);
//        chapter.setName("Chapter 1");
//        chapter.setDescription("Description");
//
//        List<Chapter> chapters = Collections.singletonList(chapter);
//
//        ApiRes<Object> apiRes = new ApiRes<>();
//        apiRes.setData(chapters);
//        //when
//        when(chapterService.findAll())
//                .thenReturn(chapters);
//        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
//                .thenReturn(apiRes);
//        //then
//        mockMvc.perform(get("/chapter/get-all")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpectAll(status().isOk(),
//                        jsonPath("$.data[0].name").value(chapter.getName()),
//                        jsonPath("$.data[0].description").value(chapter.getDescription()));
//    }
//
//    @Test
//    void testGetFeedbackByCourseIdAndType_Success() throws Exception {
//        //given
//        Chapter chapter = new Chapter();
//        chapter.setId(1L);
//        chapter.setName("Chapter 1");
//        chapter.setDescription("Description");
//
//        List<Chapter> chapters = Collections.singletonList(chapter);
//
//        ApiRes<Object> apiRes = new ApiRes<>();
//        apiRes.setData(chapters);
//        //when
//        when(chapterService.findAllByCourseId(anyLong()))
//                .thenReturn(chapters);
//        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
//                .thenReturn(apiRes);
//        //then
//        mockMvc.perform(get("/chapter/get-by-course")
//                        .param("courseId", "1")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpectAll(status().isOk(),
//                        jsonPath("$.data[0].name").value(chapter.getName()),
//                        jsonPath("$.data[0].description").value(chapter.getDescription()));
//    }
//
//    private static Chapter getChapter(CreateChapterRequest reqDto) {
//        Chapter chapter = new Chapter();
//        chapter.setName(reqDto.getName());
//        chapter.setDescription(reqDto.getDescription());
//        chapter.setCreatedDate(new Date());
//        chapter.setStatus(ChapterStatus.ACTIVE);
//        return chapter;
//    }
//}
