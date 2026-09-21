package com.medibook.slot.controller;

import com.medibook.common.response.ApiResponse;
import com.medibook.slot.dto.GenerateSlotsRequest;
import com.medibook.slot.dto.SlotResponse;
import com.medibook.slot.service.SlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyAuthority('DOCTOR','CLINIC_ADMIN','SUPER_ADMIN')")
    public ApiResponse<List<SlotResponse>> generateSlots(@Valid @RequestBody GenerateSlotsRequest request) {
        List<SlotResponse> slots = slotService.generateSlots(request);
        return ApiResponse.success("Slots generated successfully", slots);
    }

    @GetMapping("/doctor/{doctorId}/available")
    public ApiResponse<List<SlotResponse>> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<SlotResponse> slots = slotService.getAvailableSlots(doctorId, fromDate, toDate);
        return ApiResponse.success("Available slots fetched", slots);
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyAuthority('DOCTOR','CLINIC_ADMIN','SUPER_ADMIN','RECEPTIONIST')")
    public ApiResponse<List<SlotResponse>> getAllSlots(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<SlotResponse> slots = slotService.getAllSlots(doctorId, fromDate, toDate);
        return ApiResponse.success("Slots fetched", slots);
    }

    @GetMapping("/{id}")
    public ApiResponse<SlotResponse> getById(@PathVariable Long id) {
        return ApiResponse.success("Slot fetched", slotService.getById(id));
    }

    @PatchMapping("/{id}/block")
    @PreAuthorize("hasAnyAuthority('DOCTOR','CLINIC_ADMIN','SUPER_ADMIN')")
    public ApiResponse<SlotResponse> blockSlot(@PathVariable Long id) {
        return ApiResponse.success("Slot blocked", slotService.blockSlot(id));
    }

    @PatchMapping("/{id}/unblock")
    @PreAuthorize("hasAnyAuthority('DOCTOR','CLINIC_ADMIN','SUPER_ADMIN')")
    public ApiResponse<SlotResponse> unblockSlot(@PathVariable Long id) {
        return ApiResponse.success("Slot unblocked", slotService.unblockSlot(id));
    }
}