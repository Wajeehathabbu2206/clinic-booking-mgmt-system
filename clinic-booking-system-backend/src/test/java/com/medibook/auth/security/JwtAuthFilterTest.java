package com.medibook.auth.security;

import com.medibook.common.util.Role;
import com.medibook.user.entity.User;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void usesCurrentDatabaseRoleInsteadOfTokenRoleClaim() throws Exception {
        String token = "signed-token";
        User user = User.builder()
                .email("user@example.com")
                .password("encoded")
                .role(Role.PATIENT)
            .build();
        user.setId(7L);
        CustomUserDetails currentUser = new CustomUserDetails(user);
        when(jwtUtil.isTokenValid(token)).thenReturn(true);
        when(jwtUtil.extractEmail(token)).thenReturn("user@example.com");
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(currentUser);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        jwtAuthFilter.doFilterInternal(request, new MockHttpServletResponse(), mock(FilterChain.class));

        var authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        assertTrue(authorities.stream().anyMatch(authority -> "PATIENT".equals(authority.getAuthority())));
        assertFalse(authorities.stream().anyMatch(authority -> "SUPER_ADMIN".equals(authority.getAuthority())));
        verify(jwtUtil, never()).extractRole(token);
    }

    @Test
    void doesNotAuthenticateDisabledUser() throws Exception {
        String token = "signed-token";
        User user = User.builder()
                .email("user@example.com")
                .password("encoded")
                .role(Role.PATIENT)
                .enabled(false)
                .build();
        when(jwtUtil.isTokenValid(token)).thenReturn(true);
        when(jwtUtil.extractEmail(token)).thenReturn("user@example.com");
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(new CustomUserDetails(user));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        jwtAuthFilter.doFilterInternal(request, new MockHttpServletResponse(), mock(FilterChain.class));

        assertTrue(SecurityContextHolder.getContext().getAuthentication() == null);
    }
}