package com.example.confessionwall.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.UUID;

@Service
public class CsrfTokenService {
    public static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
    public static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";
    public static final String CSRF_ALT_HEADER_NAME = "X-CSRF-TOKEN";
    public static final String CSRF_TOKEN_ATTRIBUTE = "CSRF_TOKEN_ATTR";
    public static final String CSRF_COOKIE_SET_ATTRIBUTE = "CSRF_COOKIE_SET_ATTR";

    public String generateToken() {
        return UUID.randomUUID().toString();
    }

    public String extractTokenFromCookie(HttpServletRequest request) {
        if (request == null) return null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (CSRF_COOKIE_NAME.equalsIgnoreCase(cookie.getName())) {
                    String val = cookie.getValue();
                    if (val != null && !val.isBlank()) {
                        val = val.trim();
                        if (val.startsWith("\"") && val.endsWith("\"") && val.length() >= 2) {
                            val = val.substring(1, val.length() - 1);
                        }
                        return val;
                    }
                }
            }
        }
        return null;
    }

    public String extractTokenFromHeader(HttpServletRequest request) {
        if (request == null) return null;
        String header = request.getHeader(CSRF_HEADER_NAME);
        if (header != null && !header.isBlank()) {
            return cleanHeader(header);
        }
        header = request.getHeader(CSRF_ALT_HEADER_NAME);
        if (header != null && !header.isBlank()) {
            return cleanHeader(header);
        }
        return null;
    }

    private String cleanHeader(String header) {
        header = header.trim();
        if (header.startsWith("\"") && header.endsWith("\"") && header.length() >= 2) {
            header = header.substring(1, header.length() - 1);
        }
        return header;
    }

    public void addCsrfCookie(HttpServletRequest request, HttpServletResponse response, String token) {
        if (response == null || token == null || token.isBlank()) return;

        if (request != null && Boolean.TRUE.equals(request.getAttribute(CSRF_COOKIE_SET_ATTRIBUTE))) {
            return;
        }

        boolean secure = request != null && request.isSecure();

        ResponseCookie responseCookie = ResponseCookie.from(CSRF_COOKIE_NAME, token)
                .path("/")
                .sameSite("Lax")
                .httpOnly(false)
                .secure(secure)
                .maxAge(Duration.ofDays(1))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

        if (request != null) {
            request.setAttribute(CSRF_COOKIE_SET_ATTRIBUTE, Boolean.TRUE);
        }
    }

    public boolean validateToken(String cookieToken, String headerToken) {
        if (cookieToken == null || cookieToken.isBlank() ||
            headerToken == null || headerToken.isBlank() ||
            "null".equalsIgnoreCase(cookieToken) || "undefined".equalsIgnoreCase(cookieToken) ||
            "null".equalsIgnoreCase(headerToken) || "undefined".equalsIgnoreCase(headerToken)) {
            return false;
        }
        byte[] cookieBytes = cookieToken.getBytes(StandardCharsets.UTF_8);
        byte[] headerBytes = headerToken.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(cookieBytes, headerBytes);
    }
}
