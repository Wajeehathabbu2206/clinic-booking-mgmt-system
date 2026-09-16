package com.medibook.doctor.repository;

import com.medibook.doctor.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long>, JpaSpecificationExecutor<Doctor> {

    Optional<Doctor> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}