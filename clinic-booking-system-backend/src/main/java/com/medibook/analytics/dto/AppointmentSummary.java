package com.medibook.analytics.dto;

import com.medibook.appointment.entity.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentSummary(
        Long appointmentId,
        String patientName,
        String doctorName,
        String clinicName,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        AppointmentStatus status,
        String reasonForVisit
) {
}