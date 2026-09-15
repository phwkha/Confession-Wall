package com.example.confessionwall.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientIpResolverTest {

    private ClientIpResolver resolver;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        resolver = new ClientIpResolver();
        request = new MockHttpServletRequest();
    }

    @Test
    @DisplayName("Should extract IP from X-Forwarded-For header if present")
    void testXForwardedFor() {
        request.addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18");
        assertEquals("203.0.113.195", resolver.resolveClientIp(request));
    }

    @Test
    @DisplayName("Should extract IP from X-Real-IP if X-Forwarded-For is absent")
    void testXRealIp() {
        request.addHeader("X-Real-IP", "198.51.100.1");
        assertEquals("198.51.100.1", resolver.resolveClientIp(request));
    }

    @Test
    @DisplayName("Should fallback to remoteAddr if no proxy headers are present")
    void testRemoteAddrFallback() {
        request.setRemoteAddr("192.168.1.100");
        assertEquals("192.168.1.100", resolver.resolveClientIp(request));
    }

    @Test
    @DisplayName("Should normalize IPv6 loopback to 127.0.0.1")
    void testIpv6LoopbackNormalization() {
        request.setRemoteAddr("0:0:0:0:0:0:0:1");
        assertEquals("127.0.0.1", resolver.resolveClientIp(request));

        request.setRemoteAddr("::1");
        assertEquals("127.0.0.1", resolver.resolveClientIp(request));
    }

    @Test
    @DisplayName("Should return 127.0.0.1 if request is null or remoteAddr is unknown")
    void testNullOrUnknown() {
        assertEquals("127.0.0.1", resolver.resolveClientIp(null));
        request.setRemoteAddr("unknown");
        assertEquals("127.0.0.1", resolver.resolveClientIp(request));
    }
}
