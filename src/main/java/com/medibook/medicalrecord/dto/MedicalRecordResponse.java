package com.medibook.medicalrecord.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class MedicalRecordResponse {

    private Long id;
    private Long appointmentId;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private Long clinicId;
    private String clinicName;
    private String reasonForVisit;
    private String diagnosis;
    private String prescription;
    private String doctorNotes;
    private LocalDate followUpDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}