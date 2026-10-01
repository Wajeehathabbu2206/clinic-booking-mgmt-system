package com.medibook.analytics.repository;

import com.medibook.analytics.dto.AppointmentSummary;
import com.medibook.analytics.dto.ClinicAppointmentStat;
import com.medibook.analytics.dto.DailyStatusCount;
import com.medibook.analytics.dto.MedicalRecordSummary;
import com.medibook.analytics.dto.SlotStatusCount;
import com.medibook.analytics.dto.SlotTimeRow;
import com.medibook.analytics.dto.StatusCount;
import com.medibook.appointment.entity.Appointment;
import com.medibook.appointment.entity.AppointmentStatus;
import com.medibook.common.util.Role;
import com.medibook.clinic.entity.StaffStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Read-only aggregate queries for the dashboards. It is anchored on Appointment,
 * but the JPQL also reads Slot, Doctor, Clinic, User and Review.
 *
 * Optional filters (clinicId, doctorId, patientId) can be passed as null to mean "all".
 * That lets one query serve the doctor, clinic-admin and super-admin dashboards.
 * The appointment date always comes from the booked slot (slot.slotDate).
 */
public interface AnalyticsRepository extends Repository<Appointment, Long> {

    // ------------------------------------------------------------------
    // Appointment counts and trends
    // ------------------------------------------------------------------

    @Query("""
            select new com.medibook.analytics.dto.StatusCount(a.status, count(a))
            from Appointment a join a.slot s
            where (:clinicId is null or a.clinic.id = :clinicId)
              and (:doctorId is null or a.doctor.id = :doctorId)
              and (:patientId is null or a.patient.id = :patientId)
              and s.slotDate between :fromDate and :toDate
            group by a.status
            """)
    List<StatusCount> countByStatus(@Param("clinicId") Long clinicId,
                                    @Param("doctorId") Long doctorId,
                                    @Param("patientId") Long patientId,
                                    @Param("fromDate") LocalDate fromDate,
                                    @Param("toDate") LocalDate toDate);

    @Query("""
            select new com.medibook.analytics.dto.DailyStatusCount(s.slotDate, a.status, count(a))
            from Appointment a join a.slot s
            where (:clinicId is null or a.clinic.id = :clinicId)
              and (:doctorId is null or a.doctor.id = :doctorId)
              and s.slotDate between :fromDate and :toDate
            group by s.slotDate, a.status
            order by s.slotDate
            """)
    List<DailyStatusCount> countDailyByStatus(@Param("clinicId") Long clinicId,
                                              @Param("doctorId") Long doctorId,
                                              @Param("fromDate") LocalDate fromDate,
                                              @Param("toDate") LocalDate toDate);

    // ------------------------------------------------------------------
    // Revenue (consultation fee of the doctor, for COMPLETED appointments)
    // ------------------------------------------------------------------

    @Query("""
            select sum(d.consultationFee)
            from Appointment a join a.doctor d join a.slot s
            where a.status = :status
              and (:clinicId is null or a.clinic.id = :clinicId)
              and (:doctorId is null or a.doctor.id = :doctorId)
              and s.slotDate between :fromDate and :toDate
            """)
    Number sumRevenue(@Param("status") AppointmentStatus status,
                      @Param("clinicId") Long clinicId,
                      @Param("doctorId") Long doctorId,
                      @Param("fromDate") LocalDate fromDate,
                      @Param("toDate") LocalDate toDate);

    /** Rows of [LocalDate slotDate, Number revenue], ordered by date. */
    @Query("""
            select s.slotDate, sum(d.consultationFee)
            from Appointment a join a.doctor d join a.slot s
            where a.status = :status
              and (:clinicId is null or a.clinic.id = :clinicId)
              and (:doctorId is null or a.doctor.id = :doctorId)
              and s.slotDate between :fromDate and :toDate
            group by s.slotDate
            order by s.slotDate
            """)
    List<Object[]> sumRevenueDaily(@Param("status") AppointmentStatus status,
                                   @Param("clinicId") Long clinicId,
                                   @Param("doctorId") Long doctorId,
                                   @Param("fromDate") LocalDate fromDate,
                                   @Param("toDate") LocalDate toDate);

    // ------------------------------------------------------------------
    // Top doctors / clinic comparison
    // ------------------------------------------------------------------

    /**
     * Rows of [doctorId, doctorName, specialization, consultationFee,
     * totalAppointments, completedAppointments, averageRating, totalReviews],
     * ordered by total appointments (desc). Map each row with DoctorPerformanceRow.from(...).
     * Use the Pageable to limit to the top N.
     */
    @Query("""
            select d.id, u.fullName, d.specialization, d.consultationFee,
                   count(a), count(case when a.status = :completed then 1 end),
                   d.averageRating, d.totalReviews
            from Appointment a join a.doctor d join d.user u join a.slot s
            where (:clinicId is null or a.clinic.id = :clinicId)
              and s.slotDate between :fromDate and :toDate
            group by d.id, u.fullName, d.specialization, d.consultationFee,
                     d.averageRating, d.totalReviews
            order by count(a) desc
            """)
    List<Object[]> findDoctorPerformance(@Param("completed") AppointmentStatus completed,
                                         @Param("clinicId") Long clinicId,
                                         @Param("fromDate") LocalDate fromDate,
                                         @Param("toDate") LocalDate toDate,
                                         Pageable pageable);

    @Query("""
            select new com.medibook.analytics.dto.ClinicAppointmentStat(
                   c.id, c.name, count(a),
                   count(case when a.status = :completed then 1 end),
                   count(case when a.status = :cancelled then 1 end))
            from Appointment a join a.clinic c join a.slot s
            where s.slotDate between :fromDate and :toDate
            group by c.id, c.name
            order by count(a) desc
            """)
    List<ClinicAppointmentStat> findClinicComparison(@Param("completed") AppointmentStatus completed,
                                                     @Param("cancelled") AppointmentStatus cancelled,
                                                     @Param("fromDate") LocalDate fromDate,
                                                     @Param("toDate") LocalDate toDate);

    // ------------------------------------------------------------------
    // Peak days/hours and slot utilization
    // ------------------------------------------------------------------

    /** Date and start time of every non-cancelled appointment. The service buckets these by weekday and hour. */
    @Query("""
            select new com.medibook.analytics.dto.SlotTimeRow(s.slotDate, s.startTime)
            from Appointment a join a.slot s
            where a.status <> :excluded
              and (:clinicId is null or a.clinic.id = :clinicId)
              and (:doctorId is null or a.doctor.id = :doctorId)
              and s.slotDate between :fromDate and :toDate
            """)
    List<SlotTimeRow> findBookedSlotTimes(@Param("excluded") AppointmentStatus excluded,
                                          @Param("clinicId") Long clinicId,
                                          @Param("doctorId") Long doctorId,
                                          @Param("fromDate") LocalDate fromDate,
                                          @Param("toDate") LocalDate toDate);

    @Query("""
            select new com.medibook.analytics.dto.SlotStatusCount(s.status, count(s))
            from Slot s
            where (:clinicId is null or s.clinic.id = :clinicId)
              and (:doctorId is null or s.doctor.id = :doctorId)
              and s.slotDate between :fromDate and :toDate
            group by s.status
            """)
    List<SlotStatusCount> countSlotsByStatus(@Param("clinicId") Long clinicId,
                                             @Param("doctorId") Long doctorId,
                                             @Param("fromDate") LocalDate fromDate,
                                             @Param("toDate") LocalDate toDate);

    // ------------------------------------------------------------------
    // Appointment lists (patient + doctor dashboards)
    // ------------------------------------------------------------------

    /** Soonest first: today's schedule, upcoming appointments. */
    @Query("""
            select new com.medibook.analytics.dto.AppointmentSummary(
                   a.id, p.fullName, du.fullName, c.name,
                   s.slotDate, s.startTime, s.endTime, a.status, a.reasonForVisit)
            from Appointment a join a.patient p join a.doctor d join d.user du
                 join a.clinic c join a.slot s
            where a.status in :statuses
              and (:doctorId is null or d.id = :doctorId)
              and (:patientId is null or p.id = :patientId)
              and (:clinicId is null or c.id = :clinicId)
              and s.slotDate between :fromDate and :toDate
            order by s.slotDate asc, s.startTime asc
            """)
    List<AppointmentSummary> findSummariesAsc(@Param("statuses") Collection<AppointmentStatus> statuses,
                                              @Param("doctorId") Long doctorId,
                                              @Param("patientId") Long patientId,
                                              @Param("clinicId") Long clinicId,
                                              @Param("fromDate") LocalDate fromDate,
                                              @Param("toDate") LocalDate toDate,
                                              Pageable pageable);

    /** Most recent first: past visits. */
    @Query("""
            select new com.medibook.analytics.dto.AppointmentSummary(
                   a.id, p.fullName, du.fullName, c.name,
                   s.slotDate, s.startTime, s.endTime, a.status, a.reasonForVisit)
            from Appointment a join a.patient p join a.doctor d join d.user du
                 join a.clinic c join a.slot s
            where a.status in :statuses
              and (:doctorId is null or d.id = :doctorId)
              and (:patientId is null or p.id = :patientId)
              and (:clinicId is null or c.id = :clinicId)
              and s.slotDate between :fromDate and :toDate
            order by s.slotDate desc, s.startTime desc
            """)
    List<AppointmentSummary> findSummariesDesc(@Param("statuses") Collection<AppointmentStatus> statuses,
                                               @Param("doctorId") Long doctorId,
                                               @Param("patientId") Long patientId,
                                               @Param("clinicId") Long clinicId,
                                               @Param("fromDate") LocalDate fromDate,
                                               @Param("toDate") LocalDate toDate,
                                               Pageable pageable);

    /** Completed appointments of a patient that have no review yet. */
    @Query("""
            select new com.medibook.analytics.dto.AppointmentSummary(
                   a.id, p.fullName, du.fullName, c.name,
                   s.slotDate, s.startTime, s.endTime, a.status, a.reasonForVisit)
            from Appointment a join a.patient p join a.doctor d join d.user du
                 join a.clinic c join a.slot s
            where p.id = :patientId
              and a.status = :completed
              and not exists (select r.id from Review r where r.appointment.id = a.id)
            order by s.slotDate desc, s.startTime desc
            """)
    List<AppointmentSummary> findPendingReviews(@Param("patientId") Long patientId,
                                                @Param("completed") AppointmentStatus completed,
                                                Pageable pageable);

    // ------------------------------------------------------------------
    // Lookups, platform totals, ratings
    // ------------------------------------------------------------------

    @Query("select d.id from Doctor d where d.user.id = :userId")
    Optional<Long> findDoctorIdByUserId(@Param("userId") Long userId);

    @Query("select count(c) from Clinic c")
    long countClinics();

    @Query("select count(d) from Doctor d where d.isActive = true")
    long countActiveDoctors();

    @Query("select count(u) from User u where u.role = :role")
    long countUsersByRole(@Param("role") Role role);

    @Query("select avg(r.rating) from Review r")
    Double findPlatformAverageRating();

        // ------------------------------------------------------------------
    // Current-user lookup and clinic access (used by DashboardService)
    // ------------------------------------------------------------------

    @Query("select u.id from User u where u.email = :email")
    Optional<Long> findUserIdByEmail(@Param("email") String email);

    @Query("select c.name from Clinic c where c.id = :clinicId")
    Optional<String> findClinicName(@Param("clinicId") Long clinicId);

    /** True if the user created the clinic, or is an ACTIVE CLINIC_ADMIN staff member of it. */
    @Query("""
            select case when count(c) > 0 then true else false end
            from Clinic c
            where c.id = :clinicId
              and (c.createdBy.id = :userId
                   or exists (select cs.id from ClinicStaff cs
                              where cs.clinic.id = c.id
                                and cs.user.id = :userId
                                and cs.roleInClinic = :adminRole
                                and cs.status = :activeStatus))
            """)
    boolean isClinicAdmin(@Param("clinicId") Long clinicId,
                          @Param("userId") Long userId,
                          @Param("adminRole") Role adminRole,
                          @Param("activeStatus") StaffStatus activeStatus);

    // ------------------------------------------------------------------
    // Medical records (patient dashboard)
    // ------------------------------------------------------------------

    @Query("select count(mr) from MedicalRecord mr where mr.patient.id = :patientId")
    long countRecordsByPatient(@Param("patientId") Long patientId);

    @Query("""
            select new com.medibook.analytics.dto.MedicalRecordSummary(
                   mr.id, du.fullName, c.name, s.slotDate, mr.diagnosis, mr.followUpDate)
            from MedicalRecord mr join mr.doctor d join d.user du join mr.clinic c
                 join mr.appointment a join a.slot s
            where mr.patient.id = :patientId
            order by s.slotDate desc, mr.id desc
            """)
    List<MedicalRecordSummary> findRecentRecordsByPatient(@Param("patientId") Long patientId,
                                                          Pageable pageable);

    @Query("""
            select new com.medibook.analytics.dto.MedicalRecordSummary(
                   mr.id, du.fullName, c.name, s.slotDate, mr.diagnosis, mr.followUpDate)
            from MedicalRecord mr join mr.doctor d join d.user du join mr.clinic c
                 join mr.appointment a join a.slot s
            where mr.patient.id = :patientId
              and mr.followUpDate >= :today
            order by mr.followUpDate asc
            """)
    List<MedicalRecordSummary> findUpcomingFollowUps(@Param("patientId") Long patientId,
                                                     @Param("today") LocalDate today,
                                                     Pageable pageable);
}
