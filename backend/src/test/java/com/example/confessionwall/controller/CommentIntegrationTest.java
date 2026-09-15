package com.example.confessionwall.controller;

import com.example.confessionwall.dto.CommentRequest;
import com.example.confessionwall.model.Confession;
import com.example.confessionwall.repository.CommentRepository;
import com.example.confessionwall.repository.ConfessionLikeRepository;
import com.example.confessionwall.repository.ConfessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConfessionRepository confessionRepository;

    @Autowired
    private ConfessionLikeRepository confessionLikeRepository;

    @Autowired
    private CommentRepository commentRepository;

    private static final String CSRF_TOKEN = "test-csrf-token-for-comment-test";
    private static final Cookie CSRF_COOKIE = new Cookie("XSRF-TOKEN", CSRF_TOKEN);

    private Long confessionId;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        confessionLikeRepository.deleteAll();
        confessionRepository.deleteAll();

        Confession confession = new Confession();
        confession.setContent("Bài viết thú tội kiểm thử bình luận");
        confession.setAuthor("Người bí mật");
        confession.setLikes(0);
        confession.setCommentCount(0);
        confession.setCreatedAt(LocalDateTime.now());
        confession = confessionRepository.save(confession);

        confessionId = confession.getId();
    }

    @Test
    @DisplayName("GET /api/confessions/{id}/comments initially returns empty list")
    void getComments_initiallyEmpty() throws Exception {
        mockMvc.perform(get("/api/confessions/" + confessionId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("POST and GET comments end-to-end: persist, order ascending, update commentCount")
    void postAndGetComments_endToEnd() throws Exception {
        // 1. Post first comment
        CommentRequest req1 = new CommentRequest("Bình luận 1 rất chân thành", "Hoa");
        mockMvc.perform(post("/api/confessions/" + confessionId + "/comments")
                        .cookie(CSRF_COOKIE)
                        .header("X-XSRF-TOKEN", CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.confessionId", is(confessionId.intValue())))
                .andExpect(jsonPath("$.content", is("Bình luận 1 rất chân thành")))
                .andExpect(jsonPath("$.author", is("Hoa")))
                .andExpect(jsonPath("$.createdAt", notNullValue()));

        // Check confession commentCount incremented to 1
        Confession updatedConfession1 = confessionRepository.findById(confessionId).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(updatedConfession1.getCommentCount()).isEqualTo(1);

        // Wait slightly to ensure distinct timestamp
        Thread.sleep(10);

        // 2. Post second comment with blank author -> defaults to 'Ẩn danh'
        CommentRequest req2 = new CommentRequest("Bình luận 2 ẩn danh", "  ");
        mockMvc.perform(post("/api/confessions/" + confessionId + "/comments")
                        .cookie(CSRF_COOKIE)
                        .header("X-XSRF-TOKEN", CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.author", is("Ẩn danh")));

        // Check confession commentCount incremented to 2
        Confession updatedConfession2 = confessionRepository.findById(confessionId).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(updatedConfession2.getCommentCount()).isEqualTo(2);

        // 3. GET comments -> returns both ordered chronologically ascending
        mockMvc.perform(get("/api/confessions/" + confessionId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].content", is("Bình luận 1 rất chân thành")))
                .andExpect(jsonPath("$[0].author", is("Hoa")))
                .andExpect(jsonPath("$[1].content", is("Bình luận 2 ẩn danh")))
                .andExpect(jsonPath("$[1].author", is("Ẩn danh")));
    }

    @Test
    @DisplayName("POST /api/confessions/{id}/comments with blank content returns 400 Bad Request")
    void postComment_blankContent_returns400() throws Exception {
        CommentRequest blankReq = new CommentRequest("   ", "Người lạ");
        mockMvc.perform(post("/api/confessions/" + confessionId + "/comments")
                        .cookie(CSRF_COOKIE)
                        .header("X-XSRF-TOKEN", CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blankReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")));
    }

    @Test
    @DisplayName("POST /api/confessions/{id}/comments with non-existent confession ID returns 404 Not Found")
    void postComment_nonExistentId_returns404() throws Exception {
        CommentRequest req = new CommentRequest("Nội dung bình luận", "Người lạ");
        mockMvc.perform(post("/api/confessions/99999/comments")
                        .cookie(CSRF_COOKIE)
                        .header("X-XSRF-TOKEN", CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    @DisplayName("POST /api/confessions/{id}/comments with content exceeding 500 chars returns 400 Bad Request")
    void postComment_oversizedContent_returns400() throws Exception {
        CommentRequest req = new CommentRequest("A".repeat(501), "Người lạ");
        mockMvc.perform(post("/api/confessions/" + confessionId + "/comments")
                        .cookie(CSRF_COOKIE)
                        .header("X-XSRF-TOKEN", CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")));
    }

    @Test
    @DisplayName("POST /api/confessions/{id}/comments with content of exactly 500 chars returns 201 Created")
    void postComment_exact500CharsContent_returns201() throws Exception {
        CommentRequest req = new CommentRequest("A".repeat(500), "Người lạ");
        mockMvc.perform(post("/api/confessions/" + confessionId + "/comments")
                        .cookie(CSRF_COOKIE)
                        .header("X-XSRF-TOKEN", CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.content", hasLength(500)));
    }

    @Test
    @DisplayName("POST /api/confessions/{id}/comments with author exceeding 50 chars returns 400 Bad Request")
    void postComment_oversizedAuthor_returns400() throws Exception {
        CommentRequest req = new CommentRequest("Nội dung hợp lệ", "B".repeat(51));
        mockMvc.perform(post("/api/confessions/" + confessionId + "/comments")
                        .cookie(CSRF_COOKIE)
                        .header("X-XSRF-TOKEN", CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")));
    }

    @Test
    @DisplayName("POST /api/confessions/{id}/comments without CSRF token returns 403 Forbidden")
    void postComment_withoutCsrf_returns403() throws Exception {
        CommentRequest req = new CommentRequest("Bình luận không có CSRF", "Người lạ");
        mockMvc.perform(post("/api/confessions/" + confessionId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")));
    }

    @Test
    @DisplayName("GET /api/confessions/{id}/comments with non-existent confession ID returns 404 Not Found")
    void getComments_nonExistentId_returns404() throws Exception {
        mockMvc.perform(get("/api/confessions/99999/comments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }
}
