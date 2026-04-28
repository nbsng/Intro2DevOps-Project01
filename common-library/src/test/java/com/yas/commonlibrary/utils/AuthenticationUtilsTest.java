package com.yas.commonlibrary.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.AccessDeniedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collections;

class AuthenticationUtilsTest {

    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void extractUserId_withValidJwt_shouldReturnSubject() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("user-id-123");
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        String userId = AuthenticationUtils.extractUserId();

        assertEquals("user-id-123", userId);
    }

    @Test
    void extractUserId_withAnonymousAuthentication_shouldThrowAccessDeniedException() {
        AnonymousAuthenticationToken authentication = mock(AnonymousAuthenticationToken.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        assertThrows(AccessDeniedException.class, AuthenticationUtils::extractUserId);
    }

    @Test
    void extractJwt_withValidJwt_shouldReturnTokenValue() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("jwt-token-value");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        String jwtValue = AuthenticationUtils.extractJwt();

        assertEquals("jwt-token-value", jwtValue);
    }

    @Test
    void getAuthentication_shouldReturnAuthenticationFromContext() {
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        Authentication result = AuthenticationUtils.getAuthentication();

        assertEquals(authentication, result);
    }
}
