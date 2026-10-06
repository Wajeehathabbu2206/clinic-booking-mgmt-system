package com.medibook.doctor.repository;

import com.medibook.doctor.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long>, JpaSpecificationExecutor<Doctor> {

    Optional<Doctor> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    @Query("select count(d) > 0 from Doctor d where lower(trim(d.registrationNumber)) = :registrationNumber")
    boolean existsByNormalizedRegistrationNumber(@Param("registrationNumber") String registrationNumber);
}
