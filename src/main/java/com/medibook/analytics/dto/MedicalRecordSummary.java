package com.medibook.analytics.dto;

import java.time.LocalDate;

public record MedicalRecordSummary(
        Long recordId,
        String doctorName,
        String clinicName,
        LocalDate visitDate,
        String diagnosis,
        LocalDate followUpDate
) {
}