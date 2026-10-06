package com.medibook.analytics.dto;

import java.util.List;

public record PatientDashboardResponse(
        AnalyticsReport.AppointmentStats appointments,
        List<AppointmentSummary> upcomingAppointments,
        List<AppointmentSummary> pastAppointments,
        List<AppointmentSummary> pendingReviews,
        long totalMedicalRecords,
        List<MedicalRecordSummary> recentMedicalRecords,
        List<MedicalRecordSummary> upcomingFollowUps
) {
}