package com.medibook.analytics.dto;

import java.util.List;
import java.util.Map;

public record DoctorDashboardResponse(
        Long doctorId,
        List<AppointmentSummary> todaysSchedule,
        List<AppointmentSummary> upcomingAppointments,
        RatingSummary rating,
        AnalyticsReport report
) {

    public record RatingSummary(
            double averageRating,
            long totalReviews,
            Map<Integer, Long> ratingBreakdown,
            List<RecentReview> recentReviews
    ) {
    }

    public record RecentReview(Long reviewId, String patientName, Integer rating, String comment) {
    }
}