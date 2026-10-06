package com.medibook.calendar.entity;

import com.medibook.common.entity.BaseEntity;
import com.medibook.doctor.entity.Doctor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "doctor_holidays", uniqueConstraints = @UniqueConstraint(columnNames = {"doctor_id", "holiday_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorHoliday extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(length = 255)
    private String reason;
}