package com.example.confessionwall.controller;

import com.example.confessionwall.dto.ConfessionRequest;
import com.example.confessionwall.exception.GlobalExceptionHandler;
import com.example.confessionwall.exception.ResourceNotFoundException;
import com.example.confessionwall.model.Confession;
import com.example.confessionwall.service.ConfessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConfessionController.class)
@Import(GlobalExceptionHandler.class)
class ConfessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConfessionService confessionService;

    private Confession confession1;
    private Confession confession2;

    @BeforeEach
    void setUp() {
        confession1 = new Confession(1L, "Lời thú tội 1", "Ẩn danh", 5, LocalDateTime.now().minusHours(2));
        confession2 = new Confession(2L, "Lời thú tội 2", "Hương", 10, LocalDateTime.now().minusHours(1));
    }

    @Test
    @DisplayName("GET /api/confessions should return 200 OK and confessions ordered newest first")
    void getAllConfessions_shouldReturnListOrderedDesc() throws Exception {
        List<Confession> confessions = Arrays.asList(confession2, confession1);
        when(confessionService.getAllConfessions()).thenReturn(confessions);

        mockMvc.perform(get("/api/confessions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(2)))
                .andExpect(jsonPath("$[0].content", is("Lời thú tội 2")))
                .andExpect(jsonPath("$[0].author", is("Hương")))
                .andExpect(jsonPath("$[0].likes", is(10)))
                .andExpect(jsonPath("$[1].id", is(1)))
                .andExpect(jsonPath("$[1].content", is("Lời thú tội 1")));
    }

    @Test
    @DisplayName("POST /api/confessions with valid request should return 201 Created and persisted confession")
    void createConfession_validRequest_shouldReturn201Created() throws Exception {
        ConfessionRequest request = new ConfessionRequest("Học Spring Boot thật thú vị", "Lập trình viên");
        Confession createdConfession = new Confession(3L, request.getContent(), request.getAuthor(), 0, LocalDateTime.now());

        when(confessionService.createConfession(any(ConfessionRequest.class))).thenReturn(createdConfession);

        mockMvc.perform(post("/api/confessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.content", is("Học Spring Boot thật thú vị")))
                .andExpect(jsonPath("$.author", is("Lập trình viên")))
                .andExpect(jsonPath("$.likes", is(0)))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/confessions with empty or blank content should return 400 Bad Request")
    void createConfession_blankContent_shouldReturn400BadRequest() throws Exception {
        ConfessionRequest blankRequest = new ConfessionRequest("   ", "Ẩn danh");

        mockMvc.perform(post("/api/confessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blankRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Nội dung lời thú tội không được để trống")));
    }

    @Test
    @DisplayName("POST /api/confessions with null content should return 400 Bad Request")
    void createConfession_nullContent_shouldReturn400BadRequest() throws Exception {
        String invalidJson = "{\"author\":\"Ẩn danh\"}";

        mockMvc.perform(post("/api/confessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Nội dung lời thú tội không được để trống")));
    }

    @Test
    @DisplayName("POST /api/confessions with content exceeding 1000 chars should return 400 Bad Request")
    void createConfession_oversizedContent_shouldReturn400BadRequest() throws Exception {
        String longContent = "A".repeat(1001);
        ConfessionRequest oversizedRequest = new ConfessionRequest(longContent, "Ẩn danh");

        mockMvc.perform(post("/api/confessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(oversizedRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Nội dung lời thú tội không được vượt quá 1000 ký tự")));
    }

    @Test
    @DisplayName("POST /api/confessions with author exceeding 50 chars should return 400 Bad Request")
    void createConfession_oversizedAuthor_shouldReturn400BadRequest() throws Exception {
        String longAuthor = "B".repeat(51);
        ConfessionRequest oversizedRequest = new ConfessionRequest("Nội dung hợp lệ", longAuthor);

        mockMvc.perform(post("/api/confessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(oversizedRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Tên tác giả không được vượt quá 50 ký tự")));
    }

    @Test
    @DisplayName("PUT /api/confessions/{id}/like should increment likes and return 200 OK")
    void likeConfession_existingId_shouldReturn200OkWithIncrementedLikes() throws Exception {
        Confession likedConfession = new Confession(1L, "Lời thú tội 1", "Ẩn danh", 6, LocalDateTime.now());
        when(confessionService.likeConfession(eq(1L))).thenReturn(likedConfession);

        mockMvc.perform(put("/api/confessions/1/like")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.likes", is(6)));
    }

    @Test
    @DisplayName("PUT /api/confessions/{id}/like with non-existent ID should return 404 Not Found")
    void likeConfession_nonExistentId_shouldReturn404NotFound() throws Exception {
        when(confessionService.likeConfession(eq(999L)))
                .thenThrow(new ResourceNotFoundException("Lời thú tội không tồn tại với ID: 999"));

        mockMvc.perform(put("/api/confessions/999/like")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Lời thú tội không tồn tại với ID: 999")));
    }
}
