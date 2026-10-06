package com.medibook.analytics.dto;

public record ClinicAppointmentStat(
        Long clinicId,
        String clinicName,
        long totalAppointments,
        long completedAppointments,
        long cancelledAppointments
) {
}