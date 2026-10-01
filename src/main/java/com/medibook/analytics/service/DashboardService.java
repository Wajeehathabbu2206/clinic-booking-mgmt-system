package com.medibook.analytics.service;

import com.medibook.analytics.dto.*;
import com.medibook.analytics.dto.DoctorDashboardResponse.RatingSummary;
import com.medibook.analytics.dto.DoctorDashboardResponse.RecentReview;
import com.medibook.analytics.repository.AnalyticsRepository;
import com.medibook.analytics.service.AnalyticsService.DateRange;
import com.medibook.appointment.entity.AppointmentStatus;
import com.medibook.clinic.entity.StaffStatus;
import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.common.exception.UnauthorizedException;
import com.medibook.common.util.Role;
import com.medibook.review.entity.Review;
import com.medibook.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.medibook.analytics.util.AnalyticsUtil.toLong;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final LocalDate FAR_FUTURE = LocalDate.of(9999, 12, 31);

    private final AnalyticsRepository repo;
    private final AnalyticsService analytics;
    private final ReviewRepository reviewRepository;

    // ------------------------------------------------------------------
    // Patient
    // ------------------------------------------------------------------

    public PatientDashboardResponse patientDashboard(Authentication auth) {
        Long patientId = currentUserId(auth);
        LocalDate today = analytics.today();

        var stats = analytics.toStats(repo.countPatientAppointmentsByStatus(patientId));

        var upcoming = repo.findSummariesAsc(
                List.of(AppointmentStatus.BOOKED), null, patientId, null,
                today, FAR_FUTURE, PageRequest.of(0, 5));

        var past = repo.findPatientPastSummaries(
                List.of(AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED), patientId,
                today, PageRequest.of(0, 5));

        var pendingReviews = repo.findPendingReviews(
                patientId, AppointmentStatus.COMPLETED, PageRequest.of(0, 5));

        long totalRecords = repo.countRecordsByPatient(patientId);
        var recentRecords = repo.findRecentRecordsByPatient(patientId, PageRequest.of(0, 5));
        var followUps = repo.findUpcomingFollowUps(patientId, today, PageRequest.of(0, 5));

        return new PatientDashboardResponse(
                stats, upcoming, past, pendingReviews, totalRecords, recentRecords, followUps);
    }

    // ------------------------------------------------------------------
    // Doctor
    // ------------------------------------------------------------------

    public DoctorDashboardResponse doctorDashboard(Authentication auth, LocalDate from, LocalDate to,
                                                   Granularity granularity) {
        Long userId = currentUserId(auth);
        Long doctorId = repo.findDoctorIdByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for this user"));

        LocalDate today = analytics.today();
        DateRange range = analytics.normalizeRange(from, to);

        var todaysSchedule = repo.findSummariesAsc(
                List.of(AppointmentStatus.BOOKED, AppointmentStatus.COMPLETED), doctorId, null, null,
                today, today, PageRequest.of(0, 50));

        var upcoming = repo.findSummariesAsc(
                List.of(AppointmentStatus.BOOKED), doctorId, null, null,
                today.plusDays(1), FAR_FUTURE, PageRequest.of(0, 10));

        AnalyticsReport report = analytics.buildReport(null, doctorId, range, granularity, 0, false);

        return new DoctorDashboardResponse(doctorId, todaysSchedule, upcoming, ratingSummary(doctorId), report);
    }

    private RatingSummary ratingSummary(Long doctorId) {
        Double avg = reviewRepository.findAverageRatingByDoctorId(doctorId);
        double average = avg == null ? 0.0 : Math.round(avg * 10.0) / 10.0;

        Map<Integer, Long> breakdown = new LinkedHashMap<>();
        for (int star = 5; star >= 1; star--) {
            breakdown.put(star, 0L);
        }
        for (Object[] row : reviewRepository.findRatingBreakdownByDoctorId(doctorId)) {
            breakdown.put(((Number) row[0]).intValue(), toLong(row[1]));
        }

        List<RecentReview> recent = reviewRepository
                .findByDoctorId(doctorId, PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent().stream()
                .map(this::toRecentReview)
                .toList();

        return new RatingSummary(average, reviewRepository.countByDoctorId(doctorId), breakdown, recent);
    }

    private RecentReview toRecentReview(Review review) {
        return new RecentReview(
                review.getId(),
                maskPatientName(review.getPatient().getFullName()),
                review.getRating(),
                review.getComment());
    }

    private String maskPatientName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "Anonymous Patient";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0];
        }
        String lastInitial = String.valueOf(Character.toUpperCase(parts[parts.length - 1].charAt(0)));
        return parts[0] + " " + lastInitial + ".";
    }

    // ------------------------------------------------------------------
    // Clinic admin
    // ------------------------------------------------------------------

    public ClinicDashboardResponse clinicDashboard(Long clinicId, LocalDate from, LocalDate to,
                                                   Granularity granularity, int topN, Authentication auth) {
        if (!isSuperAdmin(auth)) {
            Long userId = currentUserId(auth);
            if (!repo.isClinicAdmin(clinicId, userId, Role.CLINIC_ADMIN, StaffStatus.ACTIVE)) {
                throw new UnauthorizedException("You do not have access to this clinic's dashboard");
            }
        }

        String clinicName = repo.findClinicName(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic not found with id " + clinicId));

        DateRange range = analytics.normalizeRange(from, to);
        AnalyticsReport report = analytics.buildReport(clinicId, null, range, granularity, topN, true);

        return new ClinicDashboardResponse(
                clinicId, clinicName, analytics.todayStats(clinicId, null), report);
    }

    // ------------------------------------------------------------------
    // Super admin
    // ------------------------------------------------------------------

    public AdminDashboardResponse adminDashboard(LocalDate from, LocalDate to,
                                                 Granularity granularity, int topN) {
        DateRange range = analytics.normalizeRange(from, to);
        AnalyticsReport report = analytics.buildReport(null, null, range, granularity, topN, true);

        var totals = new AdminDashboardResponse.PlatformTotals(
                repo.countClinics(),
                repo.countActiveDoctors(),
                repo.countUsersByRole(Role.PATIENT));

        Double avg = repo.findPlatformAverageRating();
        double platformAverage = avg == null ? 0.0 : Math.round(avg * 10.0) / 10.0;

        var comparison = repo.findClinicComparison(
                AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED, range.from(), range.to());

        return new AdminDashboardResponse(
                totals, platformAverage, analytics.todayStats(null, null), report, comparison);
    }

    // ------------------------------------------------------------------

    private Long currentUserId(Authentication auth) {
        return repo.findUserIdByEmail(auth.getName())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
    }

    private boolean isSuperAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> "SUPER_ADMIN".equals(a.getAuthority()));
    }
}
