package com.yas.inventory.utils;

import com.yas.commonlibrary.exception.AccessDeniedException;
import com.yas.inventory.constants.ApiConstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Nested
@DisplayName("AuthenticationUtils Tests")
class AuthenticationUtilsTest {

    // ── Constants ─────────────────────────────────────────────────────────────
    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";

    // ── Test Fixtures ───────────────────────────────────────────────────────
    private Jwt mockJwt;
    private Authentication mockAuthentication;
    private SecurityContext mockSecurityContext;

    @BeforeEach
    void setUp() {
        // Create mock JWT
        mockJwt = Jwt.withTokenValue(TEST_JWT_TOKEN)
            .header("alg", "HS256")
            .subject(TEST_USER_ID)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

        // Create mock authentication
        mockAuthentication = new JwtAuthenticationToken(mockJwt);

        // Create mock security context
        mockSecurityContext = mock(SecurityContext.class);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);

        // Set up SecurityContextHolder
        SecurityContextHolder.setContext(mockSecurityContext);
    }

    @Nested
    @DisplayName("extractUserId Tests")
    class ExtractUserIdTests {

        @Test
        @DisplayName("Should extract user ID from JWT authentication")
        void extractUserId_WithValidJwtAuthentication_ShouldReturnUserId() {
            // Act
            String userId = AuthenticationUtils.extractUserId();

            // Assert
            assertThat(userId).isEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException for anonymous authentication")
        void extractUserId_WithAnonymousAuthentication_ShouldThrowAccessDeniedException() {
            // Arrange
            Authentication anonymousAuth = mock(AnonymousAuthenticationToken.class);
            when(mockSecurityContext.getAuthentication()).thenReturn(anonymousAuth);

            // Act & Assert
            assertThatThrownBy(() -> AuthenticationUtils.extractUserId())
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining(ApiConstant.ACCESS_DENIED);
        }

        @Test
        @DisplayName("Should throw exception when authentication is null")
        void extractUserId_WithNullAuthentication_ShouldThrowNullPointerException() {
            // Arrange
            when(mockSecurityContext.getAuthentication()).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> AuthenticationUtils.extractUserId())
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("extractJwt Tests")
    class ExtractJwtTests {

        @Test
        @DisplayName("Should extract JWT token value from security context")
        void extractJwt_WithValidAuthentication_ShouldReturnJwtToken() {
            // Act
            String jwt = AuthenticationUtils.extractJwt();

            // Assert
            assertThat(jwt).isEqualTo(TEST_JWT_TOKEN);
        }

        @Test
        @DisplayName("Should throw exception when authentication is null")
        void extractJwt_WithNullAuthentication_ShouldThrowException() {
            // Arrange
            when(mockSecurityContext.getAuthentication()).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> AuthenticationUtils.extractJwt())
                .isInstanceOf(NullPointerException.class);
        }
    }
}