package com.medibook.review.controller;

import com.medibook.common.response.ApiResponse;
import com.medibook.common.response.PagedResponse;
import com.medibook.review.dto.CreateReviewRequest;
import com.medibook.review.dto.DoctorRatingSummaryResponse;
import com.medibook.review.dto.ReviewRequest;
import com.medibook.review.dto.ReviewResponse;
import com.medibook.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<ApiResponse<ReviewResponse>> create(
            @Valid @RequestBody CreateReviewRequest request,
            Authentication authentication) {
        ReviewResponse response = reviewService.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review submitted successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<ApiResponse<ReviewResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                "Review updated successfully",
                reviewService.update(id, request, authentication.getName())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        reviewService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Review fetched successfully", reviewService.getById(id)));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> doctorReviews(
            @PathVariable Long doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Reviews fetched successfully", reviewService.getDoctorReviews(doctorId, page, size)));
    }

    @GetMapping("/doctor/{doctorId}/summary")
    public ResponseEntity<ApiResponse<DoctorRatingSummaryResponse>> doctorSummary(
            @PathVariable Long doctorId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Rating summary fetched successfully", reviewService.getDoctorSummary(doctorId)));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> myReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                "Your reviews fetched successfully",
                reviewService.getMyReviews(authentication.getName(), page, size)));
    }
}