package com.example.confessionwall.security;

import com.example.confessionwall.exception.InvalidCsrfTokenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class CsrfInterceptor implements HandlerInterceptor {
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
    private final CsrfTokenService csrfTokenService;

    public CsrfInterceptor(@Autowired(required = false) CsrfTokenService csrfTokenService) {
        this.csrfTokenService = csrfTokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (csrfTokenService == null) {
            return true;
        }

        String method = request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        if (isSafeMethod(method)) {
            String token = csrfTokenService.extractTokenFromCookie(request);
            if (token == null || token.isBlank()) {
                token = csrfTokenService.generateToken();
                csrfTokenService.addCsrfCookie(request, response, token);
            }
            request.setAttribute(CsrfTokenService.CSRF_TOKEN_ATTRIBUTE, token);
            return true;
        }

        String cookieToken = csrfTokenService.extractTokenFromCookie(request);
        if (cookieToken == null || cookieToken.isBlank()) {
            throw new InvalidCsrfTokenException("Yêu cầu bị từ chối: Mã CSRF token không tồn tại trong cookie (XSRF-TOKEN).");
        }

        String headerToken = csrfTokenService.extractTokenFromHeader(request);
        if (headerToken == null || headerToken.isBlank()) {
            throw new InvalidCsrfTokenException("Yêu cầu bị từ chối: Mã CSRF token không được cung cấp trong header (X-XSRF-TOKEN).");
        }

        if (!csrfTokenService.validateToken(cookieToken, headerToken)) {
            throw new InvalidCsrfTokenException("Yêu cầu bị từ chối: Mã CSRF token không khớp hoặc không hợp lệ.");
        }

        return true;
    }

    private boolean isSafeMethod(String method) {
        return method != null && SAFE_METHODS.contains(method.toUpperCase());
    }
}
