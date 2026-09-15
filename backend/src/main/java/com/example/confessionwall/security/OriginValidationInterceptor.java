package com.example.confessionwall.security;

import com.example.confessionwall.config.CorsProperties;
import com.example.confessionwall.exception.UnauthorizedOriginException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URI;

@Component
public class OriginValidationInterceptor implements HandlerInterceptor {

    private final CorsProperties corsProperties;

    public OriginValidationInterceptor(@Autowired(required = false) CorsProperties corsProperties) {
        this.corsProperties = corsProperties != null ? corsProperties : new CorsProperties();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Allow CORS preflight requests (handled by Spring WebMvc CORS filter)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String origin = request.getHeader("Origin");
        String referer = request.getHeader("Referer");
        String secFetchSite = request.getHeader("Sec-Fetch-Site");

        // 1. Validate Origin header if present
        if (origin != null && !origin.isBlank()) {
            if ("null".equalsIgnoreCase(origin.trim()) || !corsProperties.isAllowedOrigin(origin, request)) {
                throw new UnauthorizedOriginException("Yêu cầu bị từ chối: Nguồn gốc truy cập (Origin) không được cấp phép.");
            }
            return true;
        }

        // 2. Validate Referer header if present
        if (referer != null && !referer.isBlank()) {
            String extractedOrigin = extractOriginFromReferer(referer);
            if (extractedOrigin == null || !corsProperties.isAllowedOrigin(extractedOrigin, request)) {
                throw new UnauthorizedOriginException("Yêu cầu bị từ chối: Địa chỉ chuyển tiếp (Referer) không hợp lệ.");
            }
            return true;
        }

        // 3. Inspect modern browser Fetch Metadata (Sec-Fetch-Site)
        if (secFetchSite != null && !secFetchSite.isBlank()) {
            String site = secFetchSite.trim().toLowerCase();
            if ("same-origin".equals(site) || "same-site".equals(site) || "none".equals(site)) {
                return true;
            } else if ("cross-site".equals(site)) {
                throw new UnauthorizedOriginException("Yêu cầu bị từ chối: Không cho phép yêu cầu liên trang (cross-site).");
            }
        }

        // 4. Safe direct access / internal health checks (Docker Compose, curl without origin)
        // Requests without Origin, Referer, and Sec-Fetch-Site are allowed for internal health check
        return true;
    }

    private String extractOriginFromReferer(String referer) {
        try {
            URI uri = URI.create(referer.trim());
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
        return null;
    }
}
