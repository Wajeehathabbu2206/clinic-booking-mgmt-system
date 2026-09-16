package com.medibook.clinic.controller;

import com.medibook.auth.security.CustomUserDetails;
import com.medibook.clinic.dto.*;
import com.medibook.clinic.service.ClinicService;
import com.medibook.common.response.ApiResponse;
import com.medibook.common.dto.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinics")
@RequiredArgsConstructor
public class ClinicController {

    private final ClinicService clinicService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'CLINIC_ADMIN')")
    public ResponseEntity<ApiResponse<ClinicResponse>> createClinic(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody ClinicCreateRequest request) {
        ClinicResponse response = clinicService.createClinic(request, principal.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success("Clinic created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClinicResponse>> getClinic(@PathVariable Long id) {
        ClinicResponse response = clinicService.getClinicById(id);
        return ResponseEntity.ok(ApiResponse.success("Clinic fetched successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ClinicResponse>>> getAllClinics(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<ClinicResponse> response = clinicService.getAllClinics(city, search, page, size);
        return ResponseEntity.ok(ApiResponse.success("Clinics fetched successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'CLINIC_ADMIN')")
    public ResponseEntity<ApiResponse<ClinicResponse>> updateClinic(
            @PathVariable Long id,
            @Valid @RequestBody ClinicUpdateRequest request) {
        ClinicResponse response = clinicService.updateClinic(id, request);
        return ResponseEntity.ok(ApiResponse.success("Clinic updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateClinic(@PathVariable Long id) {
        clinicService.deactivateClinic(id);
        return ResponseEntity.ok(ApiResponse.success("Clinic deactivated successfully", null));
    }

    // ---- Staff management ----

    @PostMapping("/{clinicId}/staff")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'CLINIC_ADMIN')")
    public ResponseEntity<ApiResponse<StaffResponse>> assignStaff(
            @PathVariable Long clinicId,
            @Valid @RequestBody StaffAssignRequest request) {
        StaffResponse response = clinicService.assignStaff(clinicId, request);
        return ResponseEntity.ok(ApiResponse.success("Staff assigned successfully", response));
    }

    @GetMapping("/{clinicId}/staff")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'CLINIC_ADMIN')")
    public ResponseEntity<ApiResponse<List<StaffResponse>>> getClinicStaff(@PathVariable Long clinicId) {
        List<StaffResponse> response = clinicService.getClinicStaff(clinicId);
        return ResponseEntity.ok(ApiResponse.success("Staff fetched successfully", response));
    }

    @DeleteMapping("/{clinicId}/staff/{staffId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'CLINIC_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeStaff(
            @PathVariable Long clinicId,
            @PathVariable Long staffId) {
        clinicService.removeStaff(clinicId, staffId);
        return ResponseEntity.ok(ApiResponse.success("Staff removed successfully", null));
    }
}