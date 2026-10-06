package com.medibook.appointment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RescheduleAppointmentRequest {

    @NotNull(message = "New slot ID is required")
    private Long newSlotId;
}