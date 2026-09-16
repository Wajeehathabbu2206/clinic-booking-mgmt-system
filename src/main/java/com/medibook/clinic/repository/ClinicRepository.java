package com.medibook.clinic.repository;

import com.medibook.clinic.entity.Clinic;
import com.medibook.clinic.entity.ClinicStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClinicRepository extends JpaRepository<Clinic, Long> {
    boolean existsByRegistrationNumber(String registrationNumber);
    Optional<Clinic> findByIdAndStatus(Long id, ClinicStatus status);
    Page<Clinic> findByCityIgnoreCaseAndStatus(String city, ClinicStatus status, Pageable pageable);
    Page<Clinic> findByStatus(ClinicStatus status, Pageable pageable);
    Page<Clinic> findByNameContainingIgnoreCaseAndStatus(String name, ClinicStatus status, Pageable pageable);
}