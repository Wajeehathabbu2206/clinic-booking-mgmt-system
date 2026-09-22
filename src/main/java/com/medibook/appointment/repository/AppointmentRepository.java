package com.medibook.appointment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.medibook.appointment.entity.Appointment;
import com.medibook.appointment.entity.AppointmentStatus;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    List<Appointment> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);

    List<Appointment> findByClinicIdOrderByCreatedAtDesc(Long clinicId);
    
    List<Appointment> findBySlotIdInAndStatusIn(List<Long> slotIds, List<AppointmentStatus> statuses);
}