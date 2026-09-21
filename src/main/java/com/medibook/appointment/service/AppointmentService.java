package com.medibook.appointment.service;

import com.medibook.appointment.dto.AppointmentResponse;
import com.medibook.appointment.dto.BookAppointmentRequest;
import com.medibook.appointment.dto.RescheduleAppointmentRequest;
import com.medibook.appointment.entity.Appointment;
import com.medibook.appointment.entity.AppointmentStatus;
import com.medibook.appointment.repository.AppointmentRepository;
import com.medibook.clinic.entity.Clinic;
import com.medibook.common.exception.AppointmentNotFoundException;
import com.medibook.common.exception.InvalidAppointmentStateException;
import com.medibook.common.exception.UnauthorizedException;
import com.medibook.doctor.entity.Doctor;
import com.medibook.slot.entity.Slot;
import com.medibook.slot.service.SlotService;
import com.medibook.user.entity.User;
import com.medibook.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final SlotService slotService;
    private final UserRepository userRepository;

    /**
     * Books an appointment for the given patient. Delegates slot locking to
     * SlotService.lockAndBook(), which throws SlotNotAvailableException (409)
     * if the slot is already taken/blocked. Single transaction: if anything
     * after the lock fails, the lock itself rolls back too.
     */
    
    @Transactional
    public AppointmentResponse bookAppointment(Long patientId, BookAppointmentRequest request) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new AppointmentNotFoundException("Patient not found with id: " + patientId));

        Slot slot = slotService.lockAndBook(request.getSlotId());
        Doctor doctor = slot.getDoctor();
        Clinic clinic = slot.getClinic();

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setClinic(clinic);
        appointment.setSlot(slot);
        appointment.setReasonForVisit(request.getReasonForVisit());
        appointment.setStatus(AppointmentStatus.BOOKED);

        return AppointmentResponse.fromEntity(appointmentRepository.save(appointment));
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long appointmentId, Long requesterId, String requesterRole) {
        Appointment appointment = findAppointmentOrThrow(appointmentId);
        assertCanView(appointment, requesterId, requesterRole);
        return AppointmentResponse.fromEntity(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointments(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getDoctorAppointments(Long doctorId) {
        return appointmentRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId).stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getClinicAppointments(Long clinicId) {
        return appointmentRepository.findByClinicIdOrderByCreatedAtDesc(clinicId).stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /** Cancels a BOOKED appointment and releases its slot back to AVAILABLE. */
    @Transactional
    public AppointmentResponse cancelAppointment(Long appointmentId, Long requesterId, String requesterRole) {
        Appointment appointment = findAppointmentOrThrow(appointmentId);
        assertCanModify(appointment, requesterId, requesterRole);

        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new InvalidAppointmentStateException(
                    "Only a BOOKED appointment can be cancelled. Current status: " + appointment.getStatus());
        }

        slotService.release(appointment.getSlot().getId());

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(LocalDateTime.now());
        appointment.setCancelledBy(requesterId);

        return AppointmentResponse.fromEntity(appointmentRepository.save(appointment));
    }

    /**
     * Reschedules a BOOKED appointment onto a new slot in place (same
     * appointment id; previousSlotId records what it moved from).
     *
     * Lock ordering: always touch the lower slot ID first, regardless of
     * whether it's the old or new slot. That gives every concurrent
     * reschedule call a consistent global lock order, so two reschedules
     * swapping into each other's slots can't deadlock.
     */
    @Transactional
    public AppointmentResponse rescheduleAppointment(Long appointmentId, Long requesterId, String requesterRole,
                                                       RescheduleAppointmentRequest request) {
        Appointment appointment = findAppointmentOrThrow(appointmentId);
        assertCanModify(appointment, requesterId, requesterRole);

        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new InvalidAppointmentStateException(
                    "Only a BOOKED appointment can be rescheduled. Current status: " + appointment.getStatus());
        }

        Long oldSlotId = appointment.getSlot().getId();
        Long newSlotId = request.getNewSlotId();

        if (oldSlotId.equals(newSlotId)) {
            throw new InvalidAppointmentStateException("New slot must be different from the current slot");
        }

        Slot newSlot;
        if (oldSlotId < newSlotId) {
            slotService.release(oldSlotId);
            newSlot = slotService.lockAndBook(newSlotId);
        } else {
            newSlot = slotService.lockAndBook(newSlotId);
            slotService.release(oldSlotId);
        }

        appointment.setPreviousSlotId(oldSlotId);
        appointment.setSlot(newSlot);
        appointment.setDoctor(newSlot.getDoctor());
        appointment.setClinic(newSlot.getClinic());

        return AppointmentResponse.fromEntity(appointmentRepository.save(appointment));
    }

    private Appointment findAppointmentOrThrow(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with id: " + appointmentId));
    }

    private void assertCanView(Appointment appointment, Long requesterId, String requesterRole) {
        boolean isOwner = appointment.getPatient().getId().equals(requesterId);
        boolean isPrivileged = requesterRole.equals("CLINIC_ADMIN") || requesterRole.equals("SUPER_ADMIN")
                || requesterRole.equals("DOCTOR");
        if (!isOwner && !isPrivileged) {
            throw new UnauthorizedException("You are not authorized to view this appointment");
        }
    }

    private void assertCanModify(Appointment appointment, Long requesterId, String requesterRole) {
        boolean isOwner = appointment.getPatient().getId().equals(requesterId);
        boolean isPrivileged = requesterRole.equals("CLINIC_ADMIN") || requesterRole.equals("SUPER_ADMIN");
        if (!isOwner && !isPrivileged) {
            throw new UnauthorizedException("You are not authorized to modify this appointment");
        }
    }
}