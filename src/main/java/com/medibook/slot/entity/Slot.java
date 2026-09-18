package com.medibook.slot.entity;

import com.medibook.clinic.entity.Clinic;
import com.medibook.common.entity.BaseEntity;
import com.medibook.doctor.entity.Doctor;
import com.medibook.slot.enums.SlotStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "slots",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_doctor_date_start", columnNames = {"doctor_id", "slot_date", "start_time"})
    },
    indexes = {
        @Index(name = "idx_slot_doctor_date", columnList = "doctor_id, slot_date"),
        @Index(name = "idx_slot_status", columnList = "status")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Slot extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SlotStatus status = SlotStatus.AVAILABLE;

    // Row-level lock guard — bumped on every state change via @Version,
    // acts as a secondary safety net alongside PESSIMISTIC_WRITE.
    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;
}