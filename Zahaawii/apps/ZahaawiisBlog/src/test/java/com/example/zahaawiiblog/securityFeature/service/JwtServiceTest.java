package com.example.zahaawiiblog.securityFeature.service;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", base64Secret());
        ReflectionTestUtils.setField(jwtService, "expirationSeconds", 1800L);
        ReflectionTestUtils.setField(jwtService, "cookieName", "AUTH_TOKEN");
        ReflectionTestUtils.setField(jwtService, "cookieSecure", false);
        ReflectionTestUtils.setField(jwtService, "cookieSameSite", "Lax");
    }

    @Test
    void generateTokenRoundTripsWithConfiguredSecret() {
        UserDetails userDetails = User.withUsername("alice")
                .password("secret")
                .authorities("ROLE_USER")
                .build();

        String token = jwtService.generateToken("alice");

        assertEquals("alice", jwtService.extractUsername(token));
        assertTrue(jwtService.validateToken(token, userDetails));
    }

    @Test
    void resolveTokenPrefersCookieOverBearerHeader() {
        Cookie[] cookies = {
                new Cookie("AUTH_TOKEN", "cookie-token")
        };

        String token = jwtService.resolveToken(cookies, "Bearer header-token");

        assertEquals("cookie-token", token);
    }

    @Test
    void buildAuthCookieUsesConfiguredSettings() {
        ResponseCookie cookie = jwtService.buildAuthCookie("jwt-token");

        assertEquals("AUTH_TOKEN", cookie.getName());
        assertEquals("/", cookie.getPath());
        assertTrue(cookie.isHttpOnly());
        assertFalse(cookie.isSecure());
        assertTrue(cookie.toString().contains("SameSite=Lax"));
        assertTrue(cookie.toString().contains("Max-Age=1800"));
    }

    private static String base64Secret() {
        byte[] key = Jwts.SIG.HS256.key().build().getEncoded();
        return Base64.getEncoder().encodeToString(key);
    }
}
