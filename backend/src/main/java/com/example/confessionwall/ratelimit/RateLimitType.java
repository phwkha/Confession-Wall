package com.example.confessionwall.ratelimit;

public enum RateLimitType {
    CREATE_CONFESSION(5, 60),  // Max 5 submissions per 60 seconds
    LIKE_CONFESSION(15, 60);   // Max 15 heart reactions per 60 seconds

    private final int maxRequests;
    private final int windowSeconds;

    RateLimitType(int maxRequests, int windowSeconds) {
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public int getWindowSeconds() {
        return windowSeconds;
    }
}
