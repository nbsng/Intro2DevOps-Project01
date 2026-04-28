package com.yas.recommendation.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.web.DefaultSecurityFilterChain;

class SecurityConfigTest {

    @Test
    void filterChain_shouldConfigureSecurity() throws Exception {
        SecurityConfig securityConfig = new SecurityConfig();
        
        HttpSecurity http = mock(HttpSecurity.class);
        
        // Mock chain
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.oauth2ResourceServer(any())).thenReturn(http);
        
        DefaultSecurityFilterChain mockChain = mock(DefaultSecurityFilterChain.class);
        when(http.build()).thenReturn(mockChain);

        SecurityFilterChain filterChain = securityConfig.filterChain(http);

        assertThat(filterChain).isNotNull();
    }
}
