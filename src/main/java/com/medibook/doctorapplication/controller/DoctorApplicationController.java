package com.medibook.doctorapplication.controller;

import com.medibook.common.dto.PagedResponse;
import com.medibook.common.response.ApiResponse;
import com.medibook.common.util.Role;
import com.medibook.doctorapplication.dto.DoctorApplicationRequest;
import com.medibook.doctorapplication.dto.DoctorApplicationResponse;
import com.medibook.doctorapplication.dto.ReviewRequest;
import com.medibook.doctorapplication.entity.ApplicationStatus;
import com.medibook.doctorapplication.service.DoctorApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor-applications")
@RequiredArgsConstructor
public class DoctorApplicationController {

    private final DoctorApplicationService applicationService;

    @PostMapping
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<ApiResponse<DoctorApplicationResponse>> submit(
            @Valid @RequestBody DoctorApplicationRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Doctor application submitted successfully",
                applicationService.submit(request, authentication.getName())));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DoctorApplicationResponse>>> myApplications(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Your applications fetched successfully",
                applicationService.myApplications(authentication.getName())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DoctorApplicationResponse>> getById(
            @PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Application fetched successfully",
                applicationService.getById(id, authentication.getName())));
    }

    @PatchMapping("/{id}/withdraw")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<ApiResponse<DoctorApplicationResponse>> withdraw(
            @PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Application withdrawn successfully",
                applicationService.withdraw(id, authentication.getName())));
    }

    @GetMapping("/clinic/{clinicId}")
    @PreAuthorize("hasAnyAuthority('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<DoctorApplicationResponse>>> listForClinic(
            @PathVariable Long clinicId,
            @RequestParam(defaultValue = "PENDING") ApplicationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Clinic applications fetched successfully",
                applicationService.listForClinic(clinicId, status, page, size,
                        authentication.getName(), currentRole(authentication))));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<DoctorApplicationResponse>> approve(
            @PathVariable Long id,
            @RequestBody(required = false) @Valid ReviewRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Doctor application approved successfully",
                applicationService.approve(id, request, authentication.getName(), currentRole(authentication))));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<DoctorApplicationResponse>> reject(
            @PathVariable Long id,
            @RequestBody @Valid ReviewRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Doctor application rejected successfully",
                applicationService.reject(id, request, authentication.getName(), currentRole(authentication))));
    }

    private Role currentRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .map(Role::valueOf)
                .filter(role -> role == Role.SUPER_ADMIN || role == Role.CLINIC_ADMIN)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Reviewer authority is missing"));
    }
}
