package com.medibook.review.service;

import com.medibook.appointment.entity.Appointment;
import com.medibook.appointment.entity.AppointmentStatus;
import com.medibook.appointment.repository.AppointmentRepository;
import com.medibook.common.exception.AppointmentNotFoundException;
import com.medibook.common.exception.DuplicateResourceException;
import com.medibook.common.exception.InvalidAppointmentStateException;
import com.medibook.common.exception.ReviewNotFoundException;
import com.medibook.common.exception.UnauthorizedException;
import com.medibook.common.response.PagedResponse;
import com.medibook.doctor.entity.Doctor;
import com.medibook.doctor.repository.DoctorRepository;
import com.medibook.review.dto.CreateReviewRequest;
import com.medibook.review.dto.DoctorRatingSummaryResponse;
import com.medibook.review.dto.ReviewRequest;
import com.medibook.review.dto.ReviewResponse;
import com.medibook.review.entity.Review;
import com.medibook.review.repository.ReviewRepository;
import com.medibook.user.entity.User;
import com.medibook.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponse create(CreateReviewRequest request, String currentUserEmail) {
        User currentUser = getUser(currentUserEmail);

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Appointment not found with id: " + request.getAppointmentId()));

        if (!appointment.getPatient().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only review your own appointments");
        }

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new InvalidAppointmentStateException(
                    "You can only review a COMPLETED appointment");
        }

        if (reviewRepository.existsByAppointmentId(appointment.getId())) {
            throw new DuplicateResourceException(
                    "You have already reviewed appointment id: " + appointment.getId());
        }

        Review review = Review.builder()
                .appointment(appointment)
                .patient(currentUser)
                .doctor(appointment.getDoctor())
                .rating(request.getRating())
                .comment(clean(request.getComment()))
                .build();

        Review saved = reviewRepository.saveAndFlush(review);
        refreshDoctorRating(saved.getDoctor());
        return toResponse(saved);
    }

    @Transactional
    public ReviewResponse update(Long reviewId, ReviewRequest request, String currentUserEmail) {
        User currentUser = getUser(currentUserEmail);
        Review review = findReview(reviewId);
        assertAuthor(review, currentUser);

        review.setRating(request.getRating());
        review.setComment(clean(request.getComment()));

        Review saved = reviewRepository.saveAndFlush(review);
        refreshDoctorRating(saved.getDoctor());
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long reviewId, String currentUserEmail) {
        User currentUser = getUser(currentUserEmail);
        Review review = findReview(reviewId);
        assertAuthor(review, currentUser);

        Doctor doctor = review.getDoctor();
        reviewRepository.delete(review);
        reviewRepository.flush();
        refreshDoctorRating(doctor);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getById(Long reviewId) {
        return toResponse(findReview(reviewId));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getDoctorReviews(Long doctorId, int page, int size) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ReviewNotFoundException("Doctor not found with id: " + doctorId);
        }
        return toPagedResponse(reviewRepository.findByDoctorId(doctorId, pageable(page, size)));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getMyReviews(String currentUserEmail, int page, int size) {
        User currentUser = getUser(currentUserEmail);
        return toPagedResponse(reviewRepository.findByPatientId(currentUser.getId(), pageable(page, size)));
    }

    @Transactional(readOnly = true)
    public DoctorRatingSummaryResponse getDoctorSummary(Long doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ReviewNotFoundException("Doctor not found with id: " + doctorId);
        }

        Map<Integer, Long> breakdown = new LinkedHashMap<>();
        for (int star = 5; star >= 1; star--) {
            breakdown.put(star, 0L);
        }
        for (Object[] row : reviewRepository.findRatingBreakdownByDoctorId(doctorId)) {
            breakdown.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }

        long total = reviewRepository.countByDoctorId(doctorId);
        Double avg = reviewRepository.findAverageRatingByDoctorId(doctorId);

        return DoctorRatingSummaryResponse.builder()
                .doctorId(doctorId)
                .averageRating(avg == null ? 0.0 : round1(avg))
                .totalReviews(total)
                .ratingBreakdown(breakdown)
                .build();
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    // ---------- helpers ----------

    private void assertAuthor(Review review, User user) {
        if (!review.getPatient().getId().equals(user.getId())) {
            throw new UnauthorizedException("You can only modify your own reviews");
        }
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
    }

    private void refreshDoctorRating(Doctor doctor) {
        doctor.setAverageRating(round1(reviewRepository.calculateAverageRatingByDoctorId(doctor.getId())));
        doctor.setTotalReviews(reviewRepository.countByDoctorId(doctor.getId()));
        doctorRepository.save(doctor);
    }

    private Review findReview(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + id));
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private String clean(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private String maskName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "Anonymous Patient";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0];
        }
        String lastInitial = String.valueOf(Character.toUpperCase(parts[parts.length - 1].charAt(0)));
        return parts[0] + " " + lastInitial + ".";
    }

    private PagedResponse<ReviewResponse> toPagedResponse(Page<Review> page) {
        return new PagedResponse<>(page.map(this::toResponse));
    }

    private ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .appointmentId(r.getAppointment().getId())
                .doctorId(r.getDoctor().getId())
                .doctorName(r.getDoctor().getUser().getFullName())
                .patientName(maskName(r.getPatient().getFullName()))
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
