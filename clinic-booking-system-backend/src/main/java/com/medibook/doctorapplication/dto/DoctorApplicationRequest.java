package com.medibook.doctorapplication.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DoctorApplicationRequest {

    @NotNull
    private Long clinicId;

    @NotBlank
    @Size(max = 100)
    private String specialization;

    @NotBlank
    @Size(max = 255)
    private String qualification;

    @NotNull
    @Min(0)
    @Max(60)
    private Integer experienceYears;

    @NotBlank
    @Size(max = 50)
    private String registrationNumber;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    private BigDecimal consultationFee;

    @Size(max = 2000)
    private String bio;
}
