package com.medibook.review.entity;

import com.medibook.appointment.entity.Appointment;
import com.medibook.common.entity.BaseEntity;
import com.medibook.doctor.entity.Doctor;
import com.medibook.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "reviews",
        uniqueConstraints = @UniqueConstraint(name = "uk_review_appointment", columnNames = "appointment_id"),
        indexes = {
                @Index(name = "idx_review_doctor", columnList = "doctor_id"),
                @Index(name = "idx_review_patient", columnList = "patient_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;
}