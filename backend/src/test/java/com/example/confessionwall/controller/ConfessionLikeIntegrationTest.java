package com.example.confessionwall.controller;

import com.example.confessionwall.model.Confession;
import com.example.confessionwall.repository.ConfessionLikeRepository;
import com.example.confessionwall.repository.ConfessionRepository;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConfessionLikeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConfessionRepository confessionRepository;

    @Autowired
    private ConfessionLikeRepository confessionLikeRepository;

    private static final String CSRF_TOKEN = "test-csrf-token-for-like-test";
    private static final Cookie CSRF_COOKIE = new Cookie("XSRF-TOKEN", CSRF_TOKEN);

    private Long confessionId;

    @BeforeEach
    void setUp() {
        confessionLikeRepository.deleteAll();
        confessionRepository.deleteAll();

        Confession confession = new Confession();
        confession.setContent("Lời thú tội kiểm thử tích hợp Like theo IP");
        confession.setAuthor("Ẩn danh");
        confession.setLikes(0);
        confession.setCreatedAt(LocalDateTime.now());
        confession = confessionRepository.save(confession);

        confessionId = confession.getId();
    }

    @Test
    @DisplayName("Toggle like: IP-1 likes (ON), likes again (OFF), IP-2 likes (ON) independently")
    void testToggleLikeByIpIntegration() throws Exception {
        String ip1 = "10.0.0.1";
        String ip2 = "10.0.0.2";

        // 1. IP-1 likes for the first time -> count becomes 1, liked = true
        mockMvc.perform(put("/api/confessions/" + confessionId + "/like")
                        .header("X-Forwarded-For", ip1)
                        .cookie(CSRF_COOKIE)
                        .header("X-XSRF-TOKEN", CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(confessionId.intValue())))
                .andExpect(jsonPath("$.likes", is(1)))
                .andExpect(jsonPath("$.liked", is(true)));

        // 2. IP-1 likes again -> toggle OFF -> count becomes 0, liked = false
        mockMvc.perform(put("/api/confessions/" + confessionId + "/like")
                        .header("X-Forwarded-For", ip1)
                        .cookie(CSRF_COOKIE)
                        .header("X-XSRF-TOKEN", CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(confessionId.intValue())))
                .andExpect(jsonPath("$.likes", is(0)))
                .andExpect(jsonPath("$.liked", is(false)));

        // 3. IP-1 likes again -> toggle ON -> count becomes 1, liked = true
        mockMvc.perform(put("/api/confessions/" + confessionId + "/like")
                        .header("X-Forwarded-For", ip1)
                        .cookie(CSRF_COOKIE)
                        .header("X-XSRF-TOKEN", CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(confessionId.intValue())))
                .andExpect(jsonPath("$.likes", is(1)))
                .andExpect(jsonPath("$.liked", is(true)));

        // 4. IP-2 likes -> count becomes 2, liked = true
        mockMvc.perform(put("/api/confessions/" + confessionId + "/like")
                        .header("X-Forwarded-For", ip2)
                        .cookie(CSRF_COOKIE)
                        .header("X-XSRF-TOKEN", CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(confessionId.intValue())))
                .andExpect(jsonPath("$.likes", is(2)))
                .andExpect(jsonPath("$.liked", is(true)));

        // 5. IP-1 unlikes -> count becomes 1, liked = false for IP-1
        mockMvc.perform(put("/api/confessions/" + confessionId + "/like")
                        .header("X-Forwarded-For", ip1)
                        .cookie(CSRF_COOKIE)
                        .header("X-XSRF-TOKEN", CSRF_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(confessionId.intValue())))
                .andExpect(jsonPath("$.likes", is(1)))
                .andExpect(jsonPath("$.liked", is(false)));

    }
}
