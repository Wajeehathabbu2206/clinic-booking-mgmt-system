package com.medibook.auth.service;

import com.medibook.auth.dto.RegisterRequest;
import com.medibook.auth.security.JwtUtil;
import com.medibook.common.util.Role;
import com.medibook.user.entity.User;
import com.medibook.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void registrationAlwaysCreatesPatientRole() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("A Patient");
        request.setEmail("patient@example.com");
        request.setPassword("secure-pass");

        User patient = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .role(Role.PATIENT)
                .build();
        when(userService.createUser(any(), any(), any(), any(), any())).thenReturn(patient);
        when(jwtUtil.generateToken(patient.getEmail(), Role.PATIENT.name())).thenReturn("token");

        var response = authService.register(request);

        verify(userService).createUser(
                eq("A Patient"), eq("patient@example.com"), eq("secure-pass"), eq(null), eq(Role.PATIENT));
        assertEquals(Role.PATIENT.name(), response.getRole());
    }
}