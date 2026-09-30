package com.medibook.medicalrecord.service;

import com.medibook.appointment.entity.Appointment;
import com.medibook.appointment.entity.AppointmentStatus;
import com.medibook.common.exception.AppointmentNotFoundException;
import com.medibook.common.exception.InvalidAppointmentStateException;
import com.medibook.appointment.repository.AppointmentRepository;
import com.medibook.common.exception.DuplicateResourceException;
import com.medibook.common.exception.UnauthorizedException;
import com.medibook.common.response.PagedResponse;
import com.medibook.medicalrecord.dto.CreateMedicalRecordRequest;
import com.medibook.medicalrecord.dto.MedicalRecordRequest;
import com.medibook.medicalrecord.dto.MedicalRecordResponse;
import com.medibook.medicalrecord.entity.MedicalRecord;
import com.medibook.common.exception.MedicalRecordNotFoundException;
import com.medibook.medicalrecord.repository.MedicalRecordRepository;
import com.medibook.user.entity.User;
import com.medibook.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public MedicalRecordResponse create(CreateMedicalRecordRequest request, String currentUserEmail) {
        User currentUser = getUser(currentUserEmail);

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Appointment not found with id: " + request.getAppointmentId()));

        assertTreatingDoctor(appointment, currentUser);

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new InvalidAppointmentStateException(
                    "A medical record can only be created for a COMPLETED appointment");
        }

        if (medicalRecordRepository.existsByAppointmentId(appointment.getId())) {
            throw new DuplicateResourceException(
                    "A medical record already exists for appointment id: " + appointment.getId());
        }

        MedicalRecord record = MedicalRecord.builder()
                .appointment(appointment)
                .patient(appointment.getPatient())
                .doctor(appointment.getDoctor())
                .clinic(appointment.getClinic())
                .diagnosis(request.getDiagnosis().trim())
                .prescription(clean(request.getPrescription()))
                .doctorNotes(clean(request.getDoctorNotes()))
                .followUpDate(request.getFollowUpDate())
                .build();

        return toResponse(medicalRecordRepository.save(record));
    }

    @Transactional
    public MedicalRecordResponse update(Long recordId, MedicalRecordRequest request, String currentUserEmail) {
        User currentUser = getUser(currentUserEmail);
        MedicalRecord record = findRecord(recordId);

        assertTreatingDoctor(record.getAppointment(), currentUser);

        record.setDiagnosis(request.getDiagnosis().trim());
        record.setPrescription(clean(request.getPrescription()));
        record.setDoctorNotes(clean(request.getDoctorNotes()));
        record.setFollowUpDate(request.getFollowUpDate());

        return toResponse(medicalRecordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponse getById(Long recordId, String currentUserEmail) {
        User currentUser = getUser(currentUserEmail);
        MedicalRecord record = findRecord(recordId);
        assertCanView(record, currentUser);
        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponse getByAppointmentId(Long appointmentId, String currentUserEmail) {
        User currentUser = getUser(currentUserEmail);
        MedicalRecord record = medicalRecordRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new MedicalRecordNotFoundException(
                        "No medical record found for appointment id: " + appointmentId));
        assertCanView(record, currentUser);
        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public PagedResponse<MedicalRecordResponse> getMyRecordsAsPatient(String currentUserEmail, int page, int size) {
        User currentUser = getUser(currentUserEmail);
        Page<MedicalRecord> result = medicalRecordRepository
                .findByPatientId(currentUser.getId(), pageable(page, size));
        return toPagedResponse(result);
    }

    @Transactional(readOnly = true)
    public PagedResponse<MedicalRecordResponse> getMyRecordsAsDoctor(String currentUserEmail, int page, int size) {
        User currentUser = getUser(currentUserEmail);
        Page<MedicalRecord> result = medicalRecordRepository
                .findByDoctorUserId(currentUser.getId(), pageable(page, size));
        return toPagedResponse(result);
    }

    // ---------- access rules ----------

    private void assertTreatingDoctor(Appointment appointment, User user) {
        Long doctorUserId = appointment.getDoctor().getUser().getId();
        if (!doctorUserId.equals(user.getId())) {
            throw new UnauthorizedException(
                    "Only the treating doctor can create or modify this medical record");
        }
    }

    private void assertCanView(MedicalRecord record, User user) {
        boolean isPatient = record.getPatient().getId().equals(user.getId());
        boolean isTreatingDoctor = record.getDoctor().getUser().getId().equals(user.getId());
        if (!isPatient && !isTreatingDoctor) {
            throw new UnauthorizedException("You are not allowed to view this medical record");
        }
    }

    // ---------- helpers ----------

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
    }

    private MedicalRecord findRecord(Long id) {
        return medicalRecordRepository.findById(id)
                .orElseThrow(() -> new MedicalRecordNotFoundException("Medical record not found with id: " + id));
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private String clean(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private PagedResponse<MedicalRecordResponse> toPagedResponse(Page<MedicalRecord> page) {
        return new PagedResponse<>(page.map(this::toResponse));
    }

    private MedicalRecordResponse toResponse(MedicalRecord r) {
        return MedicalRecordResponse.builder()
                .id(r.getId())
                .appointmentId(r.getAppointment().getId())
                .patientId(r.getPatient().getId())
                .patientName(r.getPatient().getFullName())
                .doctorId(r.getDoctor().getId())
                .doctorName(r.getDoctor().getUser().getFullName())
                .clinicId(r.getClinic().getId())
                .clinicName(r.getClinic().getName())
                .reasonForVisit(r.getAppointment().getReasonForVisit())
                .diagnosis(r.getDiagnosis())
                .prescription(r.getPrescription())
                .doctorNotes(r.getDoctorNotes())
                .followUpDate(r.getFollowUpDate())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
