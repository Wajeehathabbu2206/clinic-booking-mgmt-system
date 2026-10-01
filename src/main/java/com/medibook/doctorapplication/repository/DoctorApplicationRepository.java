package com.medibook.doctorapplication.repository;

import com.medibook.doctorapplication.entity.ApplicationStatus;
import com.medibook.doctorapplication.entity.DoctorApplication;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorApplicationRepository extends JpaRepository<DoctorApplication, Long> {

    boolean existsByApplicantIdAndStatus(Long applicantId, ApplicationStatus status);

    @Query("""
            select count(a) > 0 from DoctorApplication a
            where lower(trim(a.registrationNumber)) = :registrationNumber
              and a.status in :statuses
            """)
    boolean existsActiveRegistrationNumber(@Param("registrationNumber") String registrationNumber,
                                           @Param("statuses") List<ApplicationStatus> statuses);

    @Query("""
            select count(a) > 0 from DoctorApplication a
            where a.id <> :excludedId
              and lower(trim(a.registrationNumber)) = :registrationNumber
              and a.status in :statuses
            """)
    boolean existsActiveRegistrationNumberExcluding(@Param("registrationNumber") String registrationNumber,
                                                    @Param("statuses") List<ApplicationStatus> statuses,
                                                    @Param("excludedId") Long excludedId);

    @EntityGraph(attributePaths = {"applicant", "clinic", "reviewedBy", "doctor"})
    List<DoctorApplication> findByApplicantIdOrderByCreatedAtDesc(Long applicantId);

    @EntityGraph(attributePaths = {"applicant", "clinic", "reviewedBy", "doctor"})
    Optional<DoctorApplication> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"applicant", "clinic", "reviewedBy", "doctor"})
    Page<DoctorApplication> findByClinicIdAndStatus(Long clinicId, ApplicationStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from DoctorApplication a where a.id = :id")
    Optional<DoctorApplication> findByIdForUpdate(@Param("id") Long id);
}
