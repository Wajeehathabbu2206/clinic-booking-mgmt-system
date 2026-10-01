package com.medibook.analytics.service;

import com.medibook.analytics.dto.*;
import com.medibook.analytics.dto.AnalyticsReport.*;
import com.medibook.analytics.repository.AnalyticsRepository;
import com.medibook.appointment.entity.AppointmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static com.medibook.analytics.util.AnalyticsUtil.toBigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    // MediBook targets clinics in India, so "today" is computed in IST rather than the server zone.
    private static final ZoneId CLINIC_ZONE = ZoneId.of("Asia/Kolkata");
    private static final int MAX_RANGE_DAYS = 366;
    private static final int MAX_TOP_N = 20;

    private final AnalyticsRepository repo;

    public record DateRange(LocalDate from, LocalDate to) {
    }

    public LocalDate today() {
        return LocalDate.now(CLINIC_ZONE);
    }

    /**
     * Defaults to the last 30 days. If from is after to, they are swapped.
     * The range is capped at 366 days, keeping `to` and moving `from` forward.
     */
    public DateRange normalizeRange(LocalDate from, LocalDate to) {
        LocalDate end = to != null ? to : today();
        LocalDate start = from != null ? from : end.minusDays(29);
        if (start.isAfter(end)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }
        if (ChronoUnit.DAYS.between(start, end) > MAX_RANGE_DAYS - 1) {
            start = end.minusDays(MAX_RANGE_DAYS - 1);
        }
        return new DateRange(start, end);
    }

    public AppointmentStats toStats(List<StatusCount> rows) {
        long booked = 0, completed = 0, cancelled = 0;
        for (StatusCount row : rows) {
            switch (row.status()) {
                case BOOKED -> booked += row.count();
                case COMPLETED -> completed += row.count();
                case CANCELLED -> cancelled += row.count();
            }
        }
        long total = booked + completed + cancelled;
        return new AppointmentStats(
                total, booked, completed, cancelled,
                percent(completed, total), percent(cancelled, total));
    }

    /** Appointment counts for today, for a clinic and/or doctor (null = all). */
    public AppointmentStats todayStats(Long clinicId, Long doctorId) {
        LocalDate today = today();
        return toStats(repo.countByStatus(clinicId, doctorId, null, today, today));
    }

    public AnalyticsReport buildReport(Long clinicId, Long doctorId, DateRange range,
                                       Granularity granularity, int topN, boolean includeTopDoctors) {
        LocalDate from = range.from();
        LocalDate to = range.to();

        AppointmentStats stats = toStats(repo.countByStatus(clinicId, doctorId, null, from, to));

        BigDecimal totalRevenue = toBigDecimal(
                repo.sumRevenue(AppointmentStatus.COMPLETED, clinicId, doctorId, from, to));

        return new AnalyticsReport(
                from, to, granularity,
                stats,
                totalRevenue,
                appointmentTrend(clinicId, doctorId, from, to, granularity),
                revenueTrend(clinicId, doctorId, from, to, granularity),
                slotUtilization(clinicId, doctorId, from, to),
                peakBuckets(clinicId, doctorId, from, to, true),
                peakBuckets(clinicId, doctorId, from, to, false),
                includeTopDoctors ? topDoctors(clinicId, from, to, topN) : List.of()
        );
    }

    // ------------------------------------------------------------------

    private List<TrendPoint> appointmentTrend(Long clinicId, Long doctorId,
                                              LocalDate from, LocalDate to, Granularity g) {
        // key -> [booked, completed, cancelled]; pre-filled so empty periods show as zero
        Map<String, long[]> buckets = new LinkedHashMap<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            buckets.putIfAbsent(periodKey(d, g), new long[3]);
        }
        for (DailyStatusCount row : repo.countDailyByStatus(clinicId, doctorId, from, to)) {
            long[] b = buckets.computeIfAbsent(periodKey(row.date(), g), k -> new long[3]);
            switch (row.status()) {
                case BOOKED -> b[0] += row.count();
                case COMPLETED -> b[1] += row.count();
                case CANCELLED -> b[2] += row.count();
            }
        }
        List<TrendPoint> result = new ArrayList<>();
        buckets.forEach((period, b) -> result.add(new TrendPoint(period, b[0], b[1], b[2])));
        return result;
    }

    private List<RevenuePoint> revenueTrend(Long clinicId, Long doctorId,
                                            LocalDate from, LocalDate to, Granularity g) {
        Map<String, BigDecimal> buckets = new LinkedHashMap<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            buckets.putIfAbsent(periodKey(d, g), BigDecimal.ZERO);
        }
        for (Object[] row : repo.sumRevenueDaily(AppointmentStatus.COMPLETED, clinicId, doctorId, from, to)) {
            String key = periodKey((LocalDate) row[0], g);
            buckets.merge(key, toBigDecimal(row[1]), BigDecimal::add);
        }
        List<RevenuePoint> result = new ArrayList<>();
        buckets.forEach((period, revenue) -> result.add(new RevenuePoint(period, revenue)));
        return result;
    }

    private SlotUtilization slotUtilization(Long clinicId, Long doctorId, LocalDate from, LocalDate to) {
        long available = 0, booked = 0, blocked = 0, total = 0;
        for (SlotStatusCount row : repo.countSlotsByStatus(clinicId, doctorId, from, to)) {
            total += row.count();
            switch (row.status().name()) {
                case "AVAILABLE" -> available += row.count();
                case "BOOKED" -> booked += row.count();
                case "BLOCKED" -> blocked += row.count();
                default -> { }
            }
        }
        // Utilization = booked / bookable (blocked slots can't be booked, so they are excluded)
        return new SlotUtilization(total, available, booked, blocked, percent(booked, available + booked));
    }

    private List<BucketCount> peakBuckets(Long clinicId, Long doctorId,
                                          LocalDate from, LocalDate to, boolean byDay) {
        List<SlotTimeRow> rows = repo.findBookedSlotTimes(
                AppointmentStatus.CANCELLED, clinicId, doctorId, from, to);

        List<BucketCount> result = new ArrayList<>();
        if (byDay) {
            long[] counts = new long[7];
            for (SlotTimeRow r : rows) {
                counts[r.slotDate().getDayOfWeek().getValue() - 1]++;
            }
            for (int i = 0; i < 7; i++) {
                result.add(new BucketCount(DayOfWeek.of(i + 1).name(), counts[i]));
            }
        } else {
            long[] counts = new long[24];
            for (SlotTimeRow r : rows) {
                counts[r.startTime().getHour()]++;
            }
            for (int h = 0; h < 24; h++) {
                if (counts[h] > 0) {
                    result.add(new BucketCount(String.format("%02d:00", h), counts[h]));
                }
            }
        }
        return result;
    }

    private List<DoctorPerformance> topDoctors(Long clinicId, LocalDate from, LocalDate to, int topN) {
        int limit = Math.max(1, Math.min(topN, MAX_TOP_N));
        return repo.findDoctorPerformance(AppointmentStatus.COMPLETED, clinicId, from, to,
                        PageRequest.of(0, limit))
                .stream()
                .map(DoctorPerformanceRow::from)
                .map(r -> new DoctorPerformance(
                        r.doctorId(), r.doctorName(), r.specialization(), r.consultationFee(),
                        r.totalAppointments(), r.completedAppointments(),
                        r.averageRating(), r.totalReviews(),
                        (r.consultationFee() == null ? BigDecimal.ZERO : r.consultationFee())
                                .multiply(BigDecimal.valueOf(r.completedAppointments()))))
                .toList();
    }

    private static String periodKey(LocalDate date, Granularity g) {
        return switch (g) {
            case DAY -> date.toString();
            case WEEK -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString();
            case MONTH -> YearMonth.from(date).toString();
        };
    }

    private static double percent(long part, long whole) {
        return whole == 0 ? 0.0 : Math.round(part * 1000.0 / whole) / 10.0;
    }
}
