package com.example.confessionwall.config;

import com.example.confessionwall.ratelimit.RateLimitInterceptor;
import com.example.confessionwall.security.CsrfInterceptor;
import com.example.confessionwall.security.OriginValidationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;
    private final OriginValidationInterceptor originValidationInterceptor;
    private final CsrfInterceptor csrfInterceptor;
    private final CorsProperties corsProperties;

    public WebMvcConfig(
            @Autowired(required = false) RateLimitInterceptor rateLimitInterceptor,
            @Autowired(required = false) OriginValidationInterceptor originValidationInterceptor,
            @Autowired(required = false) CsrfInterceptor csrfInterceptor,
            @Autowired(required = false) CorsProperties corsProperties) {
        this.rateLimitInterceptor = rateLimitInterceptor;
        this.originValidationInterceptor = originValidationInterceptor;
        this.csrfInterceptor = csrfInterceptor;
        this.corsProperties = corsProperties;
    }

    @org.springframework.context.annotation.Bean
    public org.springframework.web.filter.CorsFilter corsFilter() {
        org.springframework.web.cors.CorsConfigurationSource source = request -> {
            String origin = request.getHeader(org.springframework.http.HttpHeaders.ORIGIN);
            if (origin == null || origin.isBlank()) {
                return null;
            }
            if (corsProperties != null && corsProperties.isAllowedOrigin(origin, request)) {
                org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
                config.addAllowedOrigin(origin);
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);
                config.setMaxAge(3600L);
                return config;
            }
            return null;
        };
        return new org.springframework.web.filter.CorsFilter(source);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origins = corsProperties != null
                ? corsProperties.getAllowedOrigins()
                : List.of("http://localhost:5173", "http://localhost:8080", "http://localhost", "http://127.0.0.1:5173", "http://127.0.0.1:8080", "http://127.0.0.1");

        registry.addMapping("/api/**")
                .allowedOrigins(origins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (originValidationInterceptor != null) {
            registry.addInterceptor(originValidationInterceptor)
                    .addPathPatterns("/api/**")
                    .order(1);
        }

        if (csrfInterceptor != null) {
            registry.addInterceptor(csrfInterceptor)
                    .addPathPatterns("/api/**")
                    .order(2);
        }

        if (rateLimitInterceptor != null) {
            registry.addInterceptor(rateLimitInterceptor)
                    .addPathPatterns("/api/confessions", "/api/confessions/**")
                    .order(3);
        }
    }
}
