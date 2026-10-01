package com.medibook.analytics.controller;

import com.medibook.analytics.dto.*;
import com.medibook.analytics.service.DashboardService;
import com.medibook.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/patient")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<ApiResponse<PatientDashboardResponse>> patientDashboard(Authentication authentication) {
        return ok(dashboardService.patientDashboard(authentication), "Patient dashboard fetched");
    }

    @GetMapping("/doctor")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<ApiResponse<DoctorDashboardResponse>> doctorDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "DAY") Granularity granularity,
            Authentication authentication) {
        return ok(dashboardService.doctorDashboard(authentication, from, to, granularity),
                "Doctor dashboard fetched");
    }

    @GetMapping("/clinic/{clinicId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'CLINIC_ADMIN')")
    public ResponseEntity<ApiResponse<ClinicDashboardResponse>> clinicDashboard(
            @PathVariable Long clinicId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "DAY") Granularity granularity,
            @RequestParam(defaultValue = "5") int topN,
            Authentication authentication) {
        return ok(dashboardService.clinicDashboard(clinicId, from, to, granularity, topN, authentication),
                "Clinic dashboard fetched");
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> adminDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "DAY") Granularity granularity,
            @RequestParam(defaultValue = "5") int topN) {
        return ok(dashboardService.adminDashboard(from, to, granularity, topN),
                "Admin dashboard fetched");
    }

    // Single place to adapt to your ApiResponse success factory
    private <T> ResponseEntity<ApiResponse<T>> ok(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }
}