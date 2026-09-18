package com.medibook.user.service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.common.exception.UnauthorizedException;
import com.medibook.common.util.Role;
import com.medibook.user.dto.UserProfileResponse;
import com.medibook.user.dto.UserProfileUpdateRequest;
import com.medibook.user.entity.User;
import com.medibook.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 🔹 Extra method (used internally)
    public User createUser(String fullName, String email, String rawPassword,
                           String phoneNumber, Role role) {

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }

        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .phoneNumber(phoneNumber)
                .role(role)
                .enabled(true)
                .accountLocked(false)
                .build();

        return userRepository.save(user);
    }
    
    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    // 🔹 Helper method 2: entity → DTO
    private UserProfileResponse toResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .build();
    }

    // 🔹 Interface methods

    @Override
    public UserProfileResponse getProfile(Long userId) {
        User user = getById(userId);
        return mapToResponse(user);
    }

    @Override
    public UserProfileResponse updateProfile(Long userId, UserProfileUpdateRequest request) {
        User user = getById(userId);

        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());

        userRepository.save(user);
        return mapToResponse(user);
    }

    @Override
    public UserProfileResponse getUserById(Long userId) {
        return mapToResponse(getById(userId));
    }

    // 🔹 Helper methods

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private UserProfileResponse mapToResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .build();
    }
    
    @Override
    @Transactional
    public UserProfileResponse updateUserRole(Long userId, Role newRole) {
        User user = findUser(userId);

        if (user.getRole() == Role.SUPER_ADMIN && newRole != Role.SUPER_ADMIN) {
            throw new UnauthorizedException("Cannot change role of a SUPER_ADMIN account");
        }

        user.setRole(newRole);
        return toResponse(userRepository.save(user));
    }
}