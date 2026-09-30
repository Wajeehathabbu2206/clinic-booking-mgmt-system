package com.medibook.medicalrecord.repository;

import com.medibook.medicalrecord.entity.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    boolean existsByAppointmentId(Long appointmentId);

    Optional<MedicalRecord> findByAppointmentId(Long appointmentId);

    Page<MedicalRecord> findByPatientId(Long patientId, Pageable pageable);

    Page<MedicalRecord> findByDoctorUserId(Long doctorUserId, Pageable pageable);
}