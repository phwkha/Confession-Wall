package com.example.confessionwall.security;

import com.example.confessionwall.model.Confession;
import com.example.confessionwall.service.ConfessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OriginValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConfessionService confessionService;

    @BeforeEach
    void setUp() {
        Confession mockConfession = new Confession(1L, "Content", "Author", 0, LocalDateTime.now());
        when(confessionService.getAllConfessions()).thenReturn(Collections.singletonList(mockConfession));
    }

    @Test
    @DisplayName("Should accept request with authorized frontend origin (http://localhost:5173)")
    void testAuthorizedOrigin_5173_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/confessions")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should accept request with authorized frontend origin (http://localhost:8080)")
    void testAuthorizedOrigin_8080_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/confessions")
                        .header("Origin", "http://localhost:8080"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should reject request with unauthorized origin (http://malicious-site.com) with HTTP 403")
    void testUnauthorizedOrigin_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/confessions")
                        .header("Origin", "http://malicious-site.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should reject request with null origin string with HTTP 403")
    void testNullOrigin_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/confessions")
                        .header("Origin", "null"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should accept request with authorized Referer (http://localhost:5173/page)")
    void testAuthorizedReferer_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/confessions")
                        .header("Referer", "http://localhost:5173/feed/123"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should reject request with unauthorized Referer with HTTP 403")
    void testUnauthorizedReferer_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/confessions")
                        .header("Referer", "http://evil-tracker.com/steal-data"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("Should reject request with malformed Referer with HTTP 403")
    void testMalformedReferer_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/confessions")
                        .header("Referer", ":::not-a-valid-uri:::"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Should accept request with Sec-Fetch-Site: same-origin")
    void testSecFetchSite_sameOrigin_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/confessions")
                        .header("Sec-Fetch-Site", "same-origin"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should accept request with Sec-Fetch-Site: same-site")
    void testSecFetchSite_sameSite_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/confessions")
                        .header("Sec-Fetch-Site", "same-site"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should accept request with Sec-Fetch-Site: none (direct browser address bar)")
    void testSecFetchSite_none_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/confessions")
                        .header("Sec-Fetch-Site", "none"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should reject request with Sec-Fetch-Site: cross-site without authorized origin with HTTP 403")
    void testSecFetchSite_crossSite_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/confessions")
                        .header("Sec-Fetch-Site", "cross-site"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("Should allow internal direct healthcheck requests (no Origin, Referer, or Sec-Fetch-Site)")
    void testDirectInternalRequest_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/confessions"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CORS Preflight OPTIONS request for authorized origin should return 200 with Access-Control-Allow-Origin")
    void testCorsPreflight_authorizedOrigin_shouldReturn200() throws Exception {
        mockMvc.perform(options("/api/confessions")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
    }

    @Test
    @DisplayName("CORS Preflight OPTIONS request for unauthorized origin should be rejected (403)")
    void testCorsPreflight_unauthorizedOrigin_shouldReturn403() throws Exception {
        mockMvc.perform(options("/api/confessions")
                        .header(HttpHeaders.ORIGIN, "http://untrusted-site.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Dynamic domain: should accept request when Origin host matches Host header (e.g. Tailscale domain)")
    void testDynamicDomain_TailscaleHostAndOrigin_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/confessions")
                        .header("Host", "kha-dev.impala-tritone.ts.net")
                        .header("Origin", "https://kha-dev.impala-tritone.ts.net"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Dynamic domain: should accept request when Referer host matches Host header")
    void testDynamicDomain_TailscaleReferer_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/confessions")
                        .header("Host", "kha-dev.impala-tritone.ts.net")
                        .header("Referer", "https://kha-dev.impala-tritone.ts.net/realtime"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Dynamic domain: should accept request when Origin host matches X-Forwarded-Host")
    void testDynamicDomain_XForwardedHostAndOrigin_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/confessions")
                        .header("X-Forwarded-Host", "kha-dev.impala-tritone.ts.net")
                        .header("Origin", "https://kha-dev.impala-tritone.ts.net"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Dynamic domain: should reject request when Origin does not match Host header")
    void testDynamicDomain_MismatchedOrigin_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/confessions")
                        .header("Host", "kha-dev.impala-tritone.ts.net")
                        .header("Origin", "http://evil.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Dynamic domain: CORS Preflight OPTIONS for same host should return 200 with matching Allow-Origin")
    void testCorsPreflight_DynamicDomain_shouldReturn200() throws Exception {
        mockMvc.perform(options("/api/confessions")
                        .header("Host", "kha-dev.impala-tritone.ts.net")
                        .header(HttpHeaders.ORIGIN, "https://kha-dev.impala-tritone.ts.net")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://kha-dev.impala-tritone.ts.net"));
    }
}
