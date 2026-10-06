package com.medibook.analytics.dto;

import java.math.BigDecimal;

import static com.medibook.analytics.util.AnalyticsUtil.*;

public record DoctorPerformanceRow(
        Long doctorId,
        String doctorName,
        String specialization,
        BigDecimal consultationFee,
        long totalAppointments,
        long completedAppointments,
        double averageRating,
        long totalReviews
) {

    /** Maps one row of AnalyticsRepository.findDoctorPerformance(...). */
    public static DoctorPerformanceRow from(Object[] r) {
        return new DoctorPerformanceRow(
                toLong(r[0]),
                (String) r[1],
                String.valueOf(r[2]),
                toBigDecimal(r[3]),
                toLong(r[4]),
                toLong(r[5]),
                toDouble(r[6]),
                toLong(r[7])
        );
    }
}