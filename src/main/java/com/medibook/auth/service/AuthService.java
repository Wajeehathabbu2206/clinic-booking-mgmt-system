package com.medibook.auth.service;

import com.medibook.auth.dto.AuthResponse;
import com.medibook.auth.dto.LoginRequest;
import com.medibook.auth.dto.RegisterRequest;
import com.medibook.auth.security.JwtUtil;
import com.medibook.user.entity.User;
import com.medibook.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        User user = userService.createUser(
                request.getFullName(),
                request.getEmail(),
                request.getPassword(),
                request.getPhoneNumber(),
                request.getRole()
        );

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userService.getByEmail(request.getEmail());
        String token = jwtUtil.generateToken(authentication);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }
}