package com.medibook.clinic.repository;

import com.medibook.clinic.entity.ClinicStaff;
import com.medibook.clinic.entity.StaffStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClinicStaffRepository extends JpaRepository<ClinicStaff, Long> {
    Optional<ClinicStaff> findByClinicIdAndUserId(Long clinicId, Long userId);
    List<ClinicStaff> findByClinicIdAndStatus(Long clinicId, StaffStatus status);
    List<ClinicStaff> findByUserIdAndStatus(Long userId, StaffStatus status);
    boolean existsByClinicIdAndUserIdAndStatus(Long clinicId, Long userId, StaffStatus status);
}