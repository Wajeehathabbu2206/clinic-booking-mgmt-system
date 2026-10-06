package com.medibook.doctorapplication.entity;

import com.medibook.clinic.entity.Clinic;
import com.medibook.common.entity.BaseEntity;
import com.medibook.doctor.entity.Doctor;
import com.medibook.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctor_applications", indexes = {
        @Index(name = "idx_doctor_application_applicant", columnList = "applicant_id"),
        @Index(name = "idx_doctor_application_clinic", columnList = "clinic_id"),
        @Index(name = "idx_doctor_application_status", columnList = "status"),
        @Index(name = "idx_doctor_application_registration", columnList = "registration_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorApplication extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @Column(nullable = false, length = 100)
    private String specialization;

    @Column(nullable = false, length = 255)
    private String qualification;

    @Column(name = "experience_years", nullable = false)
    private Integer experienceYears;

    @Column(name = "registration_number", nullable = false, length = 50)
    private String registrationNumber;

    @Column(name = "consultation_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal consultationFee;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_user_id")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_remarks", columnDefinition = "TEXT")
    private String reviewRemarks;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", unique = true)
    private Doctor doctor;
}
