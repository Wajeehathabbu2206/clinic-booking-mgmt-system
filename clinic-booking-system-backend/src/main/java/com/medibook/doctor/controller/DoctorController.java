package com.medibook.doctor.controller;

import com.medibook.common.response.ApiResponse;
import com.medibook.common.dto.PagedResponse;
import com.medibook.auth.security.CustomUserDetails;
import com.medibook.common.util.Role;
import com.medibook.doctor.dto.DoctorCreateRequest;
import com.medibook.doctor.dto.DoctorResponse;
import com.medibook.doctor.dto.DoctorUpdateRequest;
import com.medibook.doctor.dto.DoctorUserOption;
import com.medibook.doctor.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'CLINIC_ADMIN')")
        public ApiResponse<DoctorResponse> createDoctor(
                        @AuthenticationPrincipal CustomUserDetails principal,
                        @Valid @RequestBody DoctorCreateRequest request) {
        return ApiResponse.success("Doctor profile created successfully",
                                doctorService.createDoctorProfile(request, principal.getId(), Role.valueOf(principal.getRole())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'CLINIC_ADMIN', 'DOCTOR')")
    public ApiResponse<DoctorResponse> updateDoctor(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id,
            @Valid @RequestBody DoctorUpdateRequest request
    ) {
        return ApiResponse.success("Doctor profile updated successfully",
                doctorService.updateDoctorProfile(id, request, principal.getId(), Role.valueOf(principal.getRole())));
    }

    @GetMapping("/{id}")
    public ApiResponse<DoctorResponse> getDoctor(@PathVariable Long id) {
        return ApiResponse.success("Doctor fetched successfully", doctorService.getDoctorById(id));
    }

    @GetMapping("/eligible-users")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'CLINIC_ADMIN')")
    public ApiResponse<List<DoctorUserOption>> getEligibleDoctorUsers(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam Long clinicId) {
        return ApiResponse.success("Eligible doctor accounts fetched successfully",
                doctorService.getEligibleDoctorUsers(clinicId, principal.getId(), Role.valueOf(principal.getRole())));
    }

        @GetMapping("/search")
    public ApiResponse<PagedResponse<DoctorResponse>> searchDoctors(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) Long clinicId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) BigDecimal maxFee,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "experienceYears") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Role requesterRole = principal != null ? Role.valueOf(principal.getRole()) : null;
        return ApiResponse.success("Doctors fetched successfully",
                doctorService.searchDoctors(specialization, clinicId, name, minExperience, maxFee,
                        page, size, sortBy, sortDir, requesterRole));
    }

    @GetMapping("/clinic/{clinicId}")
    public ApiResponse<PagedResponse<DoctorResponse>> getDoctorsByClinic(
            @PathVariable Long clinicId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success("Doctors fetched successfully",
                doctorService.getDoctorsByClinic(clinicId, page, size));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'CLINIC_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
        public void deactivateDoctor(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
                doctorService.deactivateDoctor(id, principal.getId(), Role.valueOf(principal.getRole()));
    }
}
