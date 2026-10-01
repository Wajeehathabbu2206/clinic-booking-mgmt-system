package com.medibook.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AnalyticsReport(
        LocalDate from,
        LocalDate to,
        Granularity granularity,
        AppointmentStats appointments,
        BigDecimal totalRevenue,
        List<TrendPoint> appointmentTrend,
        List<RevenuePoint> revenueTrend,
        SlotUtilization slotUtilization,
        List<BucketCount> peakDays,
        List<BucketCount> peakHours,
        List<DoctorPerformance> topDoctors
) {

    public record AppointmentStats(
            long total,
            long booked,
            long completed,
            long cancelled,
            double completionRatePercent,
            double cancellationRatePercent
    ) {
    }

    public record TrendPoint(String period, long booked, long completed, long cancelled) {
    }

    public record RevenuePoint(String period, BigDecimal revenue) {
    }

    public record SlotUtilization(
            long totalSlots,
            long availableSlots,
            long bookedSlots,
            long blockedSlots,
            double utilizationPercent
    ) {
    }

    public record BucketCount(String label, long count) {
    }

    public record DoctorPerformance(
            Long doctorId,
            String doctorName,
            String specialization,
            BigDecimal consultationFee,
            long totalAppointments,
            long completedAppointments,
            double averageRating,
            long totalReviews,
            BigDecimal revenue
    ) {
    }
}