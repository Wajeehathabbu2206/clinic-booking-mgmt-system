package com.medibook.medicalrecord.controller;

import com.medibook.common.response.ApiResponse;
import com.medibook.common.response.PagedResponse;
import com.medibook.medicalrecord.dto.CreateMedicalRecordRequest;
import com.medibook.medicalrecord.dto.MedicalRecordRequest;
import com.medibook.medicalrecord.dto.MedicalRecordResponse;
import com.medibook.medicalrecord.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> create(
            @Valid @RequestBody CreateMedicalRecordRequest request,
            Authentication authentication) {
        MedicalRecordResponse response = medicalRecordService.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Medical record created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody MedicalRecordRequest request,
            Authentication authentication) {
        MedicalRecordResponse response = medicalRecordService.update(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Medical record updated successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PATIENT','DOCTOR')")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> getById(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                "Medical record fetched successfully",
                medicalRecordService.getById(id, authentication.getName())));
    }

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyAuthority('PATIENT','DOCTOR')")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> getByAppointment(
            @PathVariable Long appointmentId,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                "Medical record fetched successfully",
                medicalRecordService.getByAppointmentId(appointmentId, authentication.getName())));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<ApiResponse<PagedResponse<MedicalRecordResponse>>> myRecordsAsPatient(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                "Medical records fetched successfully",
                medicalRecordService.getMyRecordsAsPatient(authentication.getName(), page, size)));
    }

    @GetMapping("/doctor/my")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<ApiResponse<PagedResponse<MedicalRecordResponse>>> myRecordsAsDoctor(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                "Medical records fetched successfully",
                medicalRecordService.getMyRecordsAsDoctor(authentication.getName(), page, size)));
    }
}