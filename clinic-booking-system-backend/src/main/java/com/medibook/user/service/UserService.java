package com.medibook.user.service;

import com.medibook.common.util.Role;
import com.medibook.user.dto.UserProfileResponse;
import com.medibook.user.dto.UserProfileUpdateRequest;
import com.medibook.user.entity.User;

public interface UserService {
    UserProfileResponse getProfile(Long userId);
    UserProfileResponse updateProfile(Long userId, UserProfileUpdateRequest request);
    UserProfileResponse getUserById(Long userId);
    UserProfileResponse updateUserRole(Long userId, Role newRole);
    
    User createUser(String fullName, String email, String password,
            String phoneNumber, Role role);

User getByEmail(String email);
}