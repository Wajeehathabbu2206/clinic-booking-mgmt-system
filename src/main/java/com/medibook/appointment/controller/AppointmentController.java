package com.medibook.appointment.controller;

import com.medibook.appointment.dto.AppointmentResponse;
import com.medibook.appointment.dto.BookAppointmentRequest;
import com.medibook.appointment.dto.RescheduleAppointmentRequest;
import com.medibook.appointment.service.AppointmentService;
import com.medibook.common.response.ApiResponse;
import com.medibook.auth.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> book(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody BookAppointmentRequest request) {
        AppointmentResponse response = appointmentService.bookAppointment(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Appointment booked successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getById(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id) {
        AppointmentResponse response =
                appointmentService.getAppointmentById(id, principal.getId(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('PATIENT')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> myAppointments(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getMyAppointments(principal.getId())));
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyAuthority('DOCTOR','CLINIC_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> doctorAppointments(@PathVariable Long doctorId) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getDoctorAppointments(doctorId)));
    }

    @GetMapping("/clinic/{clinicId}")
    @PreAuthorize("hasAnyAuthority('CLINIC_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> clinicAppointments(@PathVariable Long clinicId) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getClinicAppointments(clinicId)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancel(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id) {
        AppointmentResponse response =
                appointmentService.cancelAppointment(id, principal.getId(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled", response));
    }

    @PatchMapping("/{id}/reschedule")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AppointmentResponse>> reschedule(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id,
            @Valid @RequestBody RescheduleAppointmentRequest request) {
        AppointmentResponse response =
                appointmentService.rescheduleAppointment(id, principal.getId(), principal.getRole(), request);
        return ResponseEntity.ok(ApiResponse.success("Appointment rescheduled", response));
    }
}