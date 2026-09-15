package com.example.confessionwall.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final ClientIpResolver clientIpResolver;
    private final RateLimiterService rateLimiterService;

    public RateLimitInterceptor(
            @Autowired(required = false) ClientIpResolver clientIpResolver,
            @Autowired(required = false) RateLimiterService rateLimiterService) {
        this.clientIpResolver = clientIpResolver != null ? clientIpResolver : new ClientIpResolver();
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (rateLimiterService == null) {
            return true;
        }

        String method = request.getMethod();
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        RateLimitType type = null;
        if ("POST".equalsIgnoreCase(method) && (path.equals("/api/confessions") || path.equals("/api/confessions/"))) {
            type = RateLimitType.CREATE_CONFESSION;
        } else if ("PUT".equalsIgnoreCase(method) && path.matches("^/api/confessions/\\d+/like/?$")) {
            type = RateLimitType.LIKE_CONFESSION;
        }

        if (type != null) {
            String clientIp = clientIpResolver.resolveClientIp(request);
            rateLimiterService.checkRateLimit(clientIp, type);
        }

        return true;
    }
}
