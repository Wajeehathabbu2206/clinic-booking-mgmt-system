package com.medibook.calendar.dto;

import java.time.LocalDate;
import java.util.List;

public class CalendarDayResponse {
    private LocalDate date;
    private boolean holiday;
    private String holidayReason;
    private List<CalendarSlotResponse> slots;

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public boolean isHoliday() { return holiday; }
    public void setHoliday(boolean holiday) { this.holiday = holiday; }
    public String getHolidayReason() { return holidayReason; }
    public void setHolidayReason(String holidayReason) { this.holidayReason = holidayReason; }
    public List<CalendarSlotResponse> getSlots() { return slots; }
    public void setSlots(List<CalendarSlotResponse> slots) { this.slots = slots; }
}