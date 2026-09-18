package com.medibook.slot.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateSlotsRequest {

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "From date is required")
    @FutureOrPresent(message = "From date cannot be in the past")
    private LocalDate fromDate;

    @NotNull(message = "To date is required")
    private LocalDate toDate;

    @NotNull(message = "Daily start time is required")
    private LocalTime dailyStartTime;

    @NotNull(message = "Daily end time is required")
    private LocalTime dailyEndTime;

    @NotNull(message = "Slot duration is required")
    @Min(value = 5, message = "Slot duration must be at least 5 minutes")
    @Max(value = 240, message = "Slot duration cannot exceed 240 minutes")
    private Integer slotDurationMinutes;

    // Optional — if null, slots are generated for every date in the range.
    // Otherwise only generated on these days of week (e.g. MON, WED, FRI).
    private Set<DayOfWeek> workingDays;
}