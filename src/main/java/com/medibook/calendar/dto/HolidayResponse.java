package com.medibook.calendar.dto;

import java.time.LocalDate;

public class HolidayResponse {
    private Long id;
    private Long doctorId;
    private LocalDate holidayDate;
    private String reason;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public LocalDate getHolidayDate() { return holidayDate; }
    public void setHolidayDate(LocalDate holidayDate) { this.holidayDate = holidayDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}