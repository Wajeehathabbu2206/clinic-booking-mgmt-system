package com.medibook.medicalrecord.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateMedicalRecordRequest extends MedicalRecordRequest {

    @NotNull(message = "Appointment id is required")
    private Long appointmentId;
}