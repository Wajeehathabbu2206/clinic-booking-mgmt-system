package com.medibook.calendar.dto;

import java.time.LocalTime;

public class CalendarSlotResponse {
    private Long slotId;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;        // AVAILABLE, BOOKED, COMPLETED, BLOCKED
    private String displayColor;  // suggested hex for the frontend
    private Long appointmentId;   // set if BOOKED/COMPLETED
    private String patientName;   // only set for doctor/admin/receptionist callers

    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDisplayColor() { return displayColor; }
    public void setDisplayColor(String displayColor) { this.displayColor = displayColor; }
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
}