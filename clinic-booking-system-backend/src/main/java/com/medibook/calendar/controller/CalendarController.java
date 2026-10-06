package com.medibook.calendar.controller;

import com.medibook.auth.security.CustomUserDetails;
import com.medibook.calendar.dto.*;
import com.medibook.calendar.service.CalendarService;
import com.medibook.common.response.ApiResponse;
import com.medibook.common.util.Role;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ApiResponse<CalendarResponse>> getDoctorCalendar(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Long requesterId = principal == null ? null : principal.getId();
        Role requesterRole = principal == null ? null : Role.valueOf(principal.getRole());
        CalendarResponse response = calendarService.getDoctorCalendar(
                doctorId, fromDate, toDate, requesterId, requesterRole);
        return ResponseEntity.ok(ApiResponse.success("Calendar fetched successfully", response));
    }

    @PostMapping("/doctor/{doctorId}/holidays")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('CLINIC_ADMIN') or hasAuthority('DOCTOR')")
    public ResponseEntity<ApiResponse<HolidayResponse>> addHoliday(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long doctorId,
            @Valid @RequestBody HolidayRequest request) {
        HolidayResponse response = calendarService.addHoliday(
                doctorId, request, principal.getId(), Role.valueOf(principal.getRole()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Holiday added successfully", response));
    }

    @GetMapping("/doctor/{doctorId}/holidays")
    public ResponseEntity<ApiResponse<List<HolidayResponse>>> getHolidays(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<HolidayResponse> response = calendarService.getHolidays(doctorId, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success("Holidays fetched successfully", response));
    }

    @DeleteMapping("/holidays/{holidayId}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('CLINIC_ADMIN') or hasAuthority('DOCTOR')")
    public ResponseEntity<Void> removeHoliday(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long holidayId) {
        calendarService.removeHoliday(holidayId, principal.getId(), Role.valueOf(principal.getRole()));
        return ResponseEntity.noContent().build();
    }
}