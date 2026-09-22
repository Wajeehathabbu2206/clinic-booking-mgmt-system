package com.medibook.calendar.controller;

import com.medibook.calendar.dto.*;
import com.medibook.calendar.service.CalendarService;
import com.medibook.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
            Authentication authentication) {

        boolean includePatientDetails = authentication != null && hasStaffAuthority(authentication);
        CalendarResponse response = calendarService.getDoctorCalendar(doctorId, fromDate, toDate, includePatientDetails);
        return ResponseEntity.ok(ApiResponse.success("Calendar fetched successfully", response));
    }

    @PostMapping("/doctor/{doctorId}/holidays")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('CLINIC_ADMIN') or hasAuthority('DOCTOR')")
    public ResponseEntity<ApiResponse<HolidayResponse>> addHoliday(
            @PathVariable Long doctorId,
            @Valid @RequestBody HolidayRequest request) {
        HolidayResponse response = calendarService.addHoliday(doctorId, request);
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
    public ResponseEntity<Void> removeHoliday(@PathVariable Long holidayId) {
        calendarService.removeHoliday(holidayId);
        return ResponseEntity.noContent().build();
    }

    private boolean hasStaffAuthority(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("SUPER_ADMIN")
                        || a.getAuthority().equals("CLINIC_ADMIN")
                        || a.getAuthority().equals("DOCTOR")
                        || a.getAuthority().equals("RECEPTIONIST"));
    }
}