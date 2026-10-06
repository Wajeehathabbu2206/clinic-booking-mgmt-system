package com.medibook.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookAppointmentRequest {

    @NotNull(message = "Slot ID is required")
    private Long slotId;

    @NotBlank(message = "Reason for visit is required")
    private String reasonForVisit;
}