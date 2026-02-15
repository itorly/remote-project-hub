package com.itorly.rph.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private final JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
    private final CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
    private final JwtAuthenticationFilter filter =
            new JwtAuthenticationFilter(tokenProvider, userDetailsService);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSetAuthenticationWhenTokenIsValid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer good-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(tokenProvider.getEmailFromToken("good-token")).thenReturn("user@demo.com");
        User userDetails = new User(
                "user@demo.com",
                "pw",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        when(userDetailsService.loadUserByUsername("user@demo.com")).thenReturn(userDetails);

        filter.doFilter(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo("user@demo.com");
        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldContinueWithoutAuthenticationWhenTokenIsInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        doThrow(new JwtException("bad token")).when(tokenProvider).getEmailFromToken(anyString());

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }
}
