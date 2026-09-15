package com.example.confessionwall.ratelimit;

public interface RateLimiterService {
    boolean tryAcquire(String clientIp, RateLimitType type);
    void checkRateLimit(String clientIp, RateLimitType type);
    void reset();
}
