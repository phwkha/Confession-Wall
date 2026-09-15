package com.example.confessionwall.controller;

import com.example.confessionwall.model.Confession;
import com.example.confessionwall.ratelimit.RateLimiterService;
import com.example.confessionwall.service.ConfessionService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConfessionRateLimitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RateLimiterService rateLimiterService;

    @MockBean
    private ConfessionService confessionService;

    private static final String CSRF_TOKEN = "test-csrf-token-for-ratelimit";
    private static final Cookie CSRF_COOKIE = new Cookie("XSRF-TOKEN", CSRF_TOKEN);

    @BeforeEach
    void setUp() {
        rateLimiterService.reset();
        Confession dummy = new Confession();
        dummy.setId(1L);
        dummy.setContent("Test confession");
        dummy.setAuthor("Tester");
        dummy.setLikes(0);
        when(confessionService.createConfession(any())).thenReturn(dummy);
        when(confessionService.likeConfession(eq(1L))).thenReturn(dummy);
    }

    @Test
    @DisplayName("Should return HTTP 429 Too Many Requests when creating confessions exceeds 5 requests")
    void testCreateConfessionRateLimit() throws Exception {
        String json = "{\"content\":\"Test rate limit\",\"author\":\"Tester\"}";
        String clientIp = "192.168.10.50";

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/confessions")
                            .header("X-Forwarded-For", clientIp)
                            .cookie(CSRF_COOKIE)
                            .header("X-XSRF-TOKEN", CSRF_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(post("/api/confessions")
                        .header("X-Forwarded-For", clientIp)
                        .cookie(CSRF_COOKIE)
                        .header("X-XSRF-TOKEN", CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"))
                .andExpect(jsonPath("$.message").value("Bạn đang thao tác quá nhanh, vui lòng thử lại sau."));
    }

    @Test
    @DisplayName("Should return HTTP 429 Too Many Requests when liking exceeds 15 requests")
    void testLikeConfessionRateLimit() throws Exception {
        String clientIp = "192.168.10.60";

        for (int i = 0; i < 15; i++) {
            mockMvc.perform(put("/api/confessions/1/like")
                            .header("X-Forwarded-For", clientIp)
                            .cookie(CSRF_COOKIE)
                            .header("X-XSRF-TOKEN", CSRF_TOKEN))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(put("/api/confessions/1/like")
                        .header("X-Forwarded-For", clientIp)
                        .cookie(CSRF_COOKIE)
                        .header("X-XSRF-TOKEN", CSRF_TOKEN))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"));
    }
}
