package com.medibook.user.controller;

import com.medibook.common.response.ApiResponse;
import com.medibook.user.dto.RoleUpdateRequest;
import com.medibook.user.dto.UserProfileResponse;
import com.medibook.user.dto.UserProfileUpdateRequest;
import com.medibook.user.service.UserService;
import com.medibook.auth.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails principal) {
        UserProfileResponse response = userService.getProfile(principal.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully", response));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        UserProfileResponse response = userService.updateProfile(principal.getUser().getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'CLINIC_ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable Long id) {
        UserProfileResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User fetched successfully", response));
    }
    
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequest request) {
        UserProfileResponse response = userService.updateUserRole(id, request.getRole());
        return ResponseEntity.ok(ApiResponse.success("User role updated successfully", response));
    }
}