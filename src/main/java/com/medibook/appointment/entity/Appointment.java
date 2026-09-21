package com.medibook.appointment.entity;

import com.medibook.clinic.entity.Clinic;
import com.medibook.common.entity.BaseEntity;
import com.medibook.doctor.entity.Doctor;
import com.medibook.slot.entity.Slot;
import com.medibook.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Getter
@Setter
public class Appointment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    // OneToOne + unique gives a DB-level guarantee (in addition to the
    // pessimistic lock) that a slot can never back two live appointments.
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false, unique = true)
    private Slot slot;

    @Column(name = "reason_for_visit", nullable = false, length = 500)
    private String reasonForVisit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status;

    // Set only when this appointment's slot was changed via reschedule —
    // audit trail of what slot it moved from.
    @Column(name = "previous_slot_id")
    private Long previousSlotId;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_by")
    private Long cancelledBy;
}