package com.medibook.calendar.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class HolidayRequest {

    @NotNull(message = "Holiday date is required")
    private LocalDate holidayDate;

    private String reason;

    public LocalDate getHolidayDate() { return holidayDate; }
    public void setHolidayDate(LocalDate holidayDate) { this.holidayDate = holidayDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}