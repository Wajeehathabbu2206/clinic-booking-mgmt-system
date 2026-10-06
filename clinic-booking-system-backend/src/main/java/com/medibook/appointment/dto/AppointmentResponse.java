package com.medibook.appointment.dto;

import com.medibook.appointment.entity.Appointment;
import com.medibook.appointment.entity.AppointmentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
public class AppointmentResponse {

    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private Long clinicId;
    private String clinicName;
    private Long slotId;
    private LocalDate slotDate;
    private LocalTime slotStartTime;
    private LocalTime slotEndTime;
    private String reasonForVisit;
    private AppointmentStatus status;
    private LocalDateTime createdAt;

    public static AppointmentResponse fromEntity(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getFullName())
                .doctorId(appointment.getDoctor().getId())
                .doctorName(appointment.getDoctor().getUser().getFullName())
                .clinicId(appointment.getClinic().getId())
                .clinicName(appointment.getClinic().getName())
                .slotId(appointment.getSlot().getId())
                .slotDate(appointment.getSlot().getSlotDate())
                .slotStartTime(appointment.getSlot().getStartTime())
                .slotEndTime(appointment.getSlot().getEndTime())
                .reasonForVisit(appointment.getReasonForVisit())
                .status(appointment.getStatus())
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}