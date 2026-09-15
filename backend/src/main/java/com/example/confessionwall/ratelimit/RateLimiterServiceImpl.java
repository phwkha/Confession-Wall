package com.example.confessionwall.ratelimit;

import com.example.confessionwall.exception.RateLimitExceededException;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterServiceImpl implements RateLimiterService {

    private final ConcurrentHashMap<String, Deque<Long>> requestHistory = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(String clientIp, RateLimitType type) {
        if (clientIp == null || clientIp.isBlank() || type == null) {
            return true;
        }

        String key = clientIp + ":" + type.name();
        long now = System.currentTimeMillis();
        long windowMillis = type.getWindowSeconds() * 1000L;
        int maxRequests = type.getMaxRequests();

        Deque<Long> timestamps = requestHistory.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            long windowStart = now - windowMillis;
            while (!timestamps.isEmpty() && timestamps.peekFirst() <= windowStart) {
                timestamps.pollFirst();
            }

            if (timestamps.size() < maxRequests) {
                timestamps.addLast(now);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void checkRateLimit(String clientIp, RateLimitType type) {
        if (!tryAcquire(clientIp, type)) {
            throw new RateLimitExceededException("Bạn đang thao tác quá nhanh, vui lòng thử lại sau.");
        }
    }

    @Override
    public void reset() {
        requestHistory.clear();
    }
}
