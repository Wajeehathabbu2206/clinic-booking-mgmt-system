package com.medibook.medicalrecord.entity;

import com.medibook.appointment.entity.Appointment;
import com.medibook.clinic.entity.Clinic;
import com.medibook.common.entity.BaseEntity;
import com.medibook.doctor.entity.Doctor;
import com.medibook.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "medical_records",
        uniqueConstraints = @UniqueConstraint(name = "uk_medical_record_appointment", columnNames = "appointment_id"),
        indexes = {
                @Index(name = "idx_medical_record_patient", columnList = "patient_id"),
                @Index(name = "idx_medical_record_doctor", columnList = "doctor_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecord extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String prescription;

    @Column(name = "doctor_notes", columnDefinition = "TEXT")
    private String doctorNotes;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;
}