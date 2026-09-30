package com.medibook.review.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class DoctorRatingSummaryResponse {

    private Long doctorId;
    private Double averageRating;
    private Long totalReviews;
    private Map<Integer, Long> ratingBreakdown;
}