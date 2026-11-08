package com.mediator.auth.security;

import com.mediator.auth.service.CustomUserDetailsService;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequest requestWithToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    private MockHttpServletResponse response() {
        return new MockHttpServletResponse();
    }

    @Test
    void doFilter_noAuthorizationHeader_continuesWithoutAuthentication()
            throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = response();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_nonBearerAuthorizationHeader_continuesWithoutAuthentication()
            throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc123");
        MockHttpServletResponse response = response();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void doFilter_blankBearerToken_clearsContext()
            throws ServletException, IOException {

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("old-user", null, List.of()));

        MockHttpServletRequest request = requestWithToken("   ");
        MockHttpServletResponse response = response();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService, never()).isTokenValid(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_invalidToken_clearsContext()
            throws ServletException, IOException {

        when(jwtService.isTokenValid("invalid-token")).thenReturn(false);

        MockHttpServletRequest request = requestWithToken("invalid-token");
        MockHttpServletResponse response = response();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService).isTokenValid("invalid-token");
        verifyNoInteractions(userDetailsService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_blankUsername_clearsContext()
            throws ServletException, IOException {

        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.extractUsername("valid-token")).thenReturn("  ");

        MockHttpServletRequest request = requestWithToken("valid-token");
        MockHttpServletResponse response = response();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(userDetailsService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_nullUsername_clearsContext()
            throws ServletException, IOException {

        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.extractUsername("valid-token")).thenReturn(null);

        MockHttpServletRequest request = requestWithToken("valid-token");
        MockHttpServletResponse response = response();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(userDetailsService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_validTokenAndNoExistingAuthentication_setsAuthentication()
            throws ServletException, IOException {

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("student@test.com")
                .password("password")
                .authorities("ROLE_STUDENT")
                .build();

        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.extractUsername("valid-token")).thenReturn("student@test.com");
        when(userDetailsService.loadUserByUsername("student@test.com"))
                .thenReturn(userDetails);

        MockHttpServletRequest request = requestWithToken("valid-token");
        MockHttpServletResponse response = response();

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(authentication.getPrincipal()).isEqualTo(userDetails);
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_STUDENT");

        verify(userDetailsService).loadUserByUsername("student@test.com");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_existingAuthentication_doesNotLoadUserAgain()
            throws ServletException, IOException {

        Authentication existingAuthentication = new UsernamePasswordAuthenticationToken("existing-user", null,
                List.of());

        SecurityContextHolder.getContext().setAuthentication(existingAuthentication);

        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.extractUsername("valid-token")).thenReturn("student@test.com");

        MockHttpServletRequest request = requestWithToken("valid-token");
        MockHttpServletResponse response = response();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isSameAs(existingAuthentication);

        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_jwtException_clearsContextAndContinues()
            throws ServletException, IOException {

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("old-user", null, List.of()));

        when(jwtService.isTokenValid("bad-token"))
                .thenThrow(new MalformedJwtException("Malformed token"));

        MockHttpServletRequest request = requestWithToken("bad-token");
        MockHttpServletResponse response = response();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_userNotFound_clearsContextAndContinues()
            throws ServletException, IOException {

        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.extractUsername("valid-token")).thenReturn("missing@test.com");
        when(userDetailsService.loadUserByUsername("missing@test.com"))
                .thenThrow(new UsernameNotFoundException("User not found"));

        MockHttpServletRequest request = requestWithToken("valid-token");
        MockHttpServletResponse response = response();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
