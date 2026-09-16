package com.medibook.doctor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private Long clinicId;
    private String clinicName;
    private String specialization;
    private String qualification;
    private String registrationNumber;
    private Integer experienceYears;
    private BigDecimal consultationFee;
    private String bio;
    private Boolean isActive;
    private LocalDateTime createdAt;
}