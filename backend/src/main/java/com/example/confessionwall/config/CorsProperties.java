package com.example.confessionwall.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    private List<String> allowedOrigins = new ArrayList<>(Arrays.asList(
            "http://localhost:5173",
            "http://localhost:8080",
            "http://localhost",
            "http://127.0.0.1:5173",
            "http://127.0.0.1:8080",
            "http://127.0.0.1"
    ));

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public boolean isAllowedOrigin(String origin) {
        if (origin == null || origin.isBlank()) {
            return false;
        }
        String normalized = normalizeOrigin(origin);
        for (String allowed : allowedOrigins) {
            if (normalizeOrigin(allowed).equalsIgnoreCase(normalized)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAllowedOrigin(String origin, jakarta.servlet.http.HttpServletRequest request) {
        if (origin == null || origin.isBlank()) {
            return false;
        }
        if (isAllowedOrigin(origin)) {
            return true;
        }
        return isSameHost(origin, request);
    }

    public boolean isSameHost(String origin, jakarta.servlet.http.HttpServletRequest request) {
        if (origin == null || origin.isBlank() || request == null) {
            return false;
        }
        String originHost = extractHost(origin);
        if (originHost == null || originHost.isBlank()) {
            return false;
        }
        String requestHost = extractRequestHost(request);
        if (requestHost == null || requestHost.isBlank()) {
            return false;
        }
        return originHost.equalsIgnoreCase(requestHost);
    }

    public static String extractHost(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String trimmed = url.trim();
        try {
            URI uri = URI.create(trimmed);
            String host = uri.getHost();
            if (host != null && !host.isBlank()) {
                return host.toLowerCase();
            }
        } catch (Exception ignored) {
        }
        // Fallback for raw host:port or malformed URI
        String s = trimmed.replaceFirst("^[a-zA-Z]+://", "");
        int slashIdx = s.indexOf('/');
        if (slashIdx != -1) {
            s = s.substring(0, slashIdx);
        }
        int colonIdx = s.indexOf(':');
        if (colonIdx != -1) {
            s = s.substring(0, colonIdx);
        }
        return s.toLowerCase();
    }

    public static String extractRequestHost(jakarta.servlet.http.HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        if (forwardedHost != null && !forwardedHost.isBlank()) {
            String first = forwardedHost.split(",")[0].trim();
            return extractHost("http://" + first);
        }
        String hostHeader = request.getHeader("Host");
        if (hostHeader != null && !hostHeader.isBlank()) {
            String first = hostHeader.split(",")[0].trim();
            return extractHost("http://" + first);
        }
        String serverName = request.getServerName();
        return serverName != null && !serverName.isBlank() ? serverName.toLowerCase() : null;
    }

    public static String normalizeOrigin(String origin) {
        if (origin == null) {
            return "";
        }
        String trimmed = origin.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        try {
            URI uri = URI.create(trimmed);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            int port = uri.getPort();

            if (scheme != null && host != null) {
                if ((("http".equalsIgnoreCase(scheme) && port == 80) ||
                     ("https".equalsIgnoreCase(scheme) && port == 443)) || port == -1) {
                    return scheme.toLowerCase() + "://" + host.toLowerCase();
                } else {
                    return scheme.toLowerCase() + "://" + host.toLowerCase() + ":" + port;
                }
            }
        } catch (Exception ignored) {
        }
        return trimmed.toLowerCase();
    }
}
