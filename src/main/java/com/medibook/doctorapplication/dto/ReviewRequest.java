package com.medibook.doctorapplication.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewRequest {

    @Size(max = 2000)
    private String remarks;
}
