package com.medibook.review.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReviewRequest extends ReviewRequest {

    @NotNull(message = "Appointment id is required")
    private Long appointmentId;
}