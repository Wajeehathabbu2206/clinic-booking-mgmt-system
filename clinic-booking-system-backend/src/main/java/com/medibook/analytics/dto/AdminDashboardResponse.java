package com.medibook.analytics.dto;

import java.util.List;

public record AdminDashboardResponse(
        PlatformTotals totals,
        double platformAverageRating,
        AnalyticsReport.AppointmentStats today,
        AnalyticsReport report,
        List<ClinicAppointmentStat> clinicComparison
) {

    public record PlatformTotals(long totalClinics, long totalActiveDoctors, long totalPatients) {
    }
}