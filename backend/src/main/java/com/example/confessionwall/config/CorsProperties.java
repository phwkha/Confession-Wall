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
