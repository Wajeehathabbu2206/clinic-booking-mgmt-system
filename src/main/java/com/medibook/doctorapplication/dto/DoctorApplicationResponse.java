package com.medibook.doctorapplication.dto;

import com.medibook.doctorapplication.entity.ApplicationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DoctorApplicationResponse(
        Long id,
        ApplicationStatus status,
        Applicant applicant,
        ClinicSummary clinic,
        String specialization,
        String qualification,
        Integer experienceYears,
        String registrationNumber,
        BigDecimal consultationFee,
        String bio,
        String reviewRemarks,
        String reviewedByName,
        LocalDateTime reviewedAt,
        Long doctorId,
        LocalDateTime createdAt
) {
    public record Applicant(Long id, String fullName, String email) { }
    public record ClinicSummary(Long id, String name) { }
}
