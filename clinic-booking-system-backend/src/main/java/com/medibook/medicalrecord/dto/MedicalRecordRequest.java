package com.medibook.medicalrecord.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MedicalRecordRequest {

    @NotBlank(message = "Diagnosis is required")
    @Size(max = 5000, message = "Diagnosis must be at most 5000 characters")
    private String diagnosis;

    @Size(max = 5000, message = "Prescription must be at most 5000 characters")
    private String prescription;

    @Size(max = 5000, message = "Doctor notes must be at most 5000 characters")
    private String doctorNotes;

    @FutureOrPresent(message = "Follow-up date cannot be in the past")
    private LocalDate followUpDate;
}