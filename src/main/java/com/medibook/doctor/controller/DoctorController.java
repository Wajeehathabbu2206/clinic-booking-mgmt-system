package com.medibook.doctor.controller;

import com.medibook.common.response.ApiResponse;
import com.medibook.common.dto.PagedResponse;
import com.medibook.doctor.dto.DoctorCreateRequest;
import com.medibook.doctor.dto.DoctorResponse;
import com.medibook.doctor.dto.DoctorUpdateRequest;
import com.medibook.doctor.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CLINIC_ADMIN')")
    public ApiResponse<DoctorResponse> createDoctor(@Valid @RequestBody DoctorCreateRequest request) {
        return ApiResponse.success("Doctor profile created successfully",
                doctorService.createDoctorProfile(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CLINIC_ADMIN', 'DOCTOR')")
    public ApiResponse<DoctorResponse> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorUpdateRequest request
    ) {
        return ApiResponse.success("Doctor profile updated successfully",
                doctorService.updateDoctorProfile(id, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<DoctorResponse> getDoctor(@PathVariable Long id) {
        return ApiResponse.success("Doctor fetched successfully", doctorService.getDoctorById(id));
    }

    @GetMapping("/search")
    public ApiResponse<PagedResponse<DoctorResponse>> searchDoctors(
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
        return ApiResponse.success("Doctors fetched successfully",
                doctorService.searchDoctors(specialization, clinicId, name, minExperience, maxFee,
                        page, size, sortBy, sortDir));
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CLINIC_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateDoctor(@PathVariable Long id) {
        doctorService.deactivateDoctor(id);
    }
}