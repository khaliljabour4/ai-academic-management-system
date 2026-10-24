package com.codewithkahlil.backend;

import com.codewithkahlil.backend.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "SECRET_KEY",
                "0000000000000000000000000000000000000000000000000000000000test01");
        user = new User("student@school.ma", "pw", List.of());
    }

    @Test
    void generatedTokenContainsUsernameAndIsValid() {
        String token = jwtService.generateToken(user);
        assertEquals("student@school.ma", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void tokenIsNotValidForAnotherUser() {
        String token = jwtService.generateToken(user);
        UserDetails other = new User("other@school.ma", "pw", List.of());
        assertFalse(jwtService.isTokenValid(token, other));
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = jwtService.generateToken(user);
        String tampered = token.substring(0, token.length() - 2) + "xx";
        assertThrows(Exception.class, () -> jwtService.extractUsername(tampered));
    }
}
