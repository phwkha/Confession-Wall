package com.example.confessionwall.controller;

import com.example.confessionwall.dto.CsrfTokenResponse;
import com.example.confessionwall.security.CsrfTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/csrf")
public class CsrfController {
    private final CsrfTokenService csrfTokenService;

    public CsrfController(CsrfTokenService csrfTokenService) {
        this.csrfTokenService = csrfTokenService;
    }

    @GetMapping
    public ResponseEntity<CsrfTokenResponse> getCsrfToken(HttpServletRequest request, HttpServletResponse response) {
        String token = (String) request.getAttribute(CsrfTokenService.CSRF_TOKEN_ATTRIBUTE);
        if (token == null || token.isBlank()) {
            token = csrfTokenService.extractTokenFromCookie(request);
        }
        if (token == null || token.isBlank()) {
            token = csrfTokenService.generateToken();
            csrfTokenService.addCsrfCookie(request, response, token);
        }

        return ResponseEntity.ok(new CsrfTokenResponse(
                token,
                CsrfTokenService.CSRF_HEADER_NAME,
                CsrfTokenService.CSRF_COOKIE_NAME
        ));
    }
}
