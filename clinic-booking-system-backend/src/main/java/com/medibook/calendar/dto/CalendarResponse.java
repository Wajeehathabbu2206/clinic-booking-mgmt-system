package com.medibook.calendar.dto;

import java.time.LocalDate;
import java.util.List;

public class CalendarResponse {
    private Long doctorId;
    private String doctorName;
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<CalendarDayResponse> days;

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    public List<CalendarDayResponse> getDays() { return days; }
    public void setDays(List<CalendarDayResponse> days) { this.days = days; }
}