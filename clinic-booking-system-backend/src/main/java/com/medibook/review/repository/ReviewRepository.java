package com.medibook.review.repository;

import com.medibook.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByAppointmentId(Long appointmentId);

    Page<Review> findByDoctorId(Long doctorId, Pageable pageable);

    Page<Review> findByPatientId(Long patientId, Pageable pageable);

    long countByDoctorId(Long doctorId);

    @Query("select coalesce(avg(r.rating), 0.0) from Review r where r.doctor.id = :doctorId")
    Double calculateAverageRatingByDoctorId(@Param("doctorId") Long doctorId);

    @Query("select avg(r.rating) from Review r where r.doctor.id = :doctorId")
    Double findAverageRatingByDoctorId(@Param("doctorId") Long doctorId);

    @Query("select r.rating, count(r) from Review r where r.doctor.id = :doctorId group by r.rating")
    List<Object[]> findRatingBreakdownByDoctorId(@Param("doctorId") Long doctorId);
}
