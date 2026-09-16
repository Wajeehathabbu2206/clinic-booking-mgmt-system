package com.medibook.doctor.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DoctorCreateRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Clinic ID is required")
    private Long clinicId;

    @NotBlank(message = "Specialization is required")
    @Size(max = 100)
    private String specialization;

    @Size(max = 255)
    private String qualification;

    @Size(max = 50)
    private String registrationNumber;

    @Min(value = 0, message = "Experience cannot be negative")
    private Integer experienceYears;

    @DecimalMin(value = "0.0", message = "Consultation fee cannot be negative")
    private BigDecimal consultationFee;

    @Size(max = 2000)
    private String bio;
}