package com.example.confessionwall.ratelimit;

import com.example.confessionwall.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterServiceTest {

    private RateLimiterServiceImpl rateLimiterService;

    @BeforeEach
    void setUp() {
        rateLimiterService = new RateLimiterServiceImpl();
    }

    @Test
    @DisplayName("Should allow requests up to maximum threshold")
    void testTryAcquireWithinLimit() {
        String ip = "10.0.0.1";
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiterService.tryAcquire(ip, RateLimitType.CREATE_CONFESSION));
        }
        assertFalse(rateLimiterService.tryAcquire(ip, RateLimitType.CREATE_CONFESSION));
    }

    @Test
    @DisplayName("Should isolate rate limits across different IP addresses")
    void testDifferentIpsIsolated() {
        String ip1 = "10.0.0.1";
        String ip2 = "10.0.0.2";

        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiterService.tryAcquire(ip1, RateLimitType.CREATE_CONFESSION));
        }
        assertFalse(rateLimiterService.tryAcquire(ip1, RateLimitType.CREATE_CONFESSION));

        assertTrue(rateLimiterService.tryAcquire(ip2, RateLimitType.CREATE_CONFESSION));
    }

    @Test
    @DisplayName("Should throw RateLimitExceededException when checkRateLimit is exceeded")
    void testCheckRateLimitThrows() {
        String ip = "10.0.0.3";
        for (int i = 0; i < 5; i++) {
            rateLimiterService.checkRateLimit(ip, RateLimitType.CREATE_CONFESSION);
        }

        RateLimitExceededException exception = assertThrows(RateLimitExceededException.class, () -> {
            rateLimiterService.checkRateLimit(ip, RateLimitType.CREATE_CONFESSION);
        });
        assertTrue(exception.getMessage().contains("Bạn đang thao tác quá nhanh"));
    }

    @Test
    @DisplayName("Should reset history when reset is called")
    void testReset() {
        String ip = "10.0.0.4";
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiterService.tryAcquire(ip, RateLimitType.CREATE_CONFESSION));
        }
        assertFalse(rateLimiterService.tryAcquire(ip, RateLimitType.CREATE_CONFESSION));

        rateLimiterService.reset();
        assertTrue(rateLimiterService.tryAcquire(ip, RateLimitType.CREATE_CONFESSION));
    }
}
