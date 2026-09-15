package com.example.confessionwall.security;

import com.example.confessionwall.model.Confession;
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

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CsrfProtectionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConfessionService confessionService;

    private static final String VALID_TOKEN = "valid-test-csrf-token-12345";
    private static final Cookie CSRF_COOKIE = new Cookie("XSRF-TOKEN", VALID_TOKEN);

    @BeforeEach
    void setUp() {
        Confession dummy = new Confession(1L, "Test Content", "Tester", 0, LocalDateTime.now());
        when(confessionService.createConfession(any())).thenReturn(dummy);
        when(confessionService.likeConfession(eq(1L))).thenReturn(dummy);
    }

    @Test
    @DisplayName("GET /api/csrf should return 200 OK and issue XSRF-TOKEN cookie")
    void testGetCsrf_shouldReturnTokenAndSetCookie() throws Exception {
        mockMvc.perform(get("/api/csrf"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.headerName").value("X-XSRF-TOKEN"))
                .andExpect(jsonPath("$.cookieName").value("XSRF-TOKEN"));
    }

    @Test
    @DisplayName("Safe request GET /api/confessions should succeed without CSRF token")
    void testSafeGetRequest_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/confessions"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("XSRF-TOKEN"));
    }

    @Test
    @DisplayName("POST /api/confessions without CSRF token should return HTTP 403 Forbidden")
    void testPostWithoutCsrf_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/confessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Hello\",\"author\":\"Anon\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message", containsString("CSRF")));
    }

    @Test
    @DisplayName("POST /api/confessions with cookie but missing X-XSRF-TOKEN header should return HTTP 403")
    void testPostWithCookieOnly_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/confessions")
                        .cookie(CSRF_COOKIE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Hello\",\"author\":\"Anon\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("POST /api/confessions with mismatched CSRF token should return HTTP 403")
    void testPostWithMismatchedToken_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/confessions")
                        .cookie(CSRF_COOKIE)
                        .header("X-XSRF-TOKEN", "wrong-token-value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Hello\",\"author\":\"Anon\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("POST /api/confessions with stringified null or undefined token should return HTTP 403")
    void testPostWithNullStringToken_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/confessions")
                        .cookie(new Cookie("XSRF-TOKEN", "null"))
                        .header("X-XSRF-TOKEN", "null")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Hello\",\"author\":\"Anon\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("POST /api/confessions with valid matching CSRF token should return HTTP 201 Created")
    void testPostWithValidCsrfToken_shouldReturn201() throws Exception {
        mockMvc.perform(post("/api/confessions")
                        .cookie(CSRF_COOKIE)
                        .header("X-XSRF-TOKEN", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Hello\",\"author\":\"Anon\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /api/confessions/{id}/like without CSRF token should return HTTP 403")
    void testLikeWithoutCsrf_shouldReturn403() throws Exception {
        mockMvc.perform(put("/api/confessions/1/like"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("PUT /api/confessions/{id}/like with valid matching CSRF token should return HTTP 200 OK")
    void testLikeWithValidCsrfToken_shouldReturn200() throws Exception {
        mockMvc.perform(put("/api/confessions/1/like")
                        .cookie(CSRF_COOKIE)
                        .header("X-XSRF-TOKEN", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Mutating request with alternative X-CSRF-TOKEN header should also succeed")
    void testAltCsrfHeader_shouldReturn200() throws Exception {
        mockMvc.perform(put("/api/confessions/1/like")
                        .cookie(CSRF_COOKIE)
                        .header("X-CSRF-TOKEN", VALID_TOKEN))
                .andExpect(status().isOk());
    }
}
