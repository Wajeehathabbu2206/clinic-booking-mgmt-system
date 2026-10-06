package com.medibook.doctor.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DoctorUpdateRequest {

    @Size(max = 100)
    private String specialization;

    @Size(max = 255)
    private String qualification;

    @Size(max = 50)
    private String registrationNumber;

    @Min(0)
    private Integer experienceYears;

    @DecimalMin("0.0")
    private BigDecimal consultationFee;

    @Size(max = 2000)
    private String bio;

    private Boolean isActive;
}