package com.medibook.user.controller;

import com.medibook.common.response.ApiResponse;
import com.medibook.common.dto.PagedResponse;
import com.medibook.user.dto.RoleUpdateRequest;
import com.medibook.user.dto.UserProfileResponse;
import com.medibook.user.dto.UserProfileUpdateRequest;
import com.medibook.user.dto.UserPromotionCandidate;
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

    @GetMapping("/promotion-candidates")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<UserPromotionCandidate>>> getPromotionCandidates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Promotion candidates fetched successfully",
                userService.getClinicOwnerPromotionCandidates(page, size)));
    }

    @PostMapping("/{id}/promote-clinic-owner")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> promoteToClinicOwner(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User promoted to clinic owner successfully",
                userService.promoteToClinicOwner(id)));
    }

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
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
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
