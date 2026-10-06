package com.medibook.analytics.dto;

public record ClinicDashboardResponse(
        Long clinicId,
        String clinicName,
        AnalyticsReport.AppointmentStats today,
        AnalyticsReport report
) {
}