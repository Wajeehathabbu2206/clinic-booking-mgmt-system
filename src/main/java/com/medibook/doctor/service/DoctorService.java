package com.medibook.doctor.service;

import com.medibook.clinic.entity.Clinic;
import com.medibook.clinic.repository.ClinicRepository;
import com.medibook.common.dto.PagedResponse;
import com.medibook.common.exception.DuplicateResourceException;
import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.common.exception.UnauthorizedException;
import com.medibook.doctor.dto.DoctorCreateRequest;
import com.medibook.doctor.dto.DoctorResponse;
import com.medibook.doctor.dto.DoctorUpdateRequest;
import com.medibook.doctor.entity.Doctor;
import com.medibook.doctor.repository.DoctorRepository;
import com.medibook.doctor.repository.DoctorSpecification;
import com.medibook.common.util.Role;
import com.medibook.user.entity.User;
import com.medibook.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final ClinicRepository clinicRepository;

    @Transactional
    public DoctorResponse createDoctorProfile(DoctorCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        if (user.getRole() != Role.DOCTOR) {
            throw new UnauthorizedException("User must have DOCTOR role to create a doctor profile");
        }

        if (doctorRepository.existsByUserId(user.getId())) {
            throw new DuplicateResourceException("A doctor profile already exists for this user");
        }

        Clinic clinic = clinicRepository.findById(request.getClinicId())
                .orElseThrow(() -> new ResourceNotFoundException("Clinic not found with id: " + request.getClinicId()));

        Doctor doctor = Doctor.builder()
                .user(user)
                .clinic(clinic)
                .specialization(request.getSpecialization())
                .qualification(request.getQualification())
                .registrationNumber(request.getRegistrationNumber())
                .experienceYears(request.getExperienceYears())
                .consultationFee(request.getConsultationFee())
                .bio(request.getBio())
                .isActive(true)
                .build();

        Doctor saved = doctorRepository.save(doctor);
        return mapToResponse(saved);
    }

    @Transactional
    public DoctorResponse updateDoctorProfile(Long doctorId, DoctorUpdateRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        if (request.getSpecialization() != null) doctor.setSpecialization(request.getSpecialization());
        if (request.getQualification() != null) doctor.setQualification(request.getQualification());
        if (request.getRegistrationNumber() != null) doctor.setRegistrationNumber(request.getRegistrationNumber());
        if (request.getExperienceYears() != null) doctor.setExperienceYears(request.getExperienceYears());
        if (request.getConsultationFee() != null) doctor.setConsultationFee(request.getConsultationFee());
        if (request.getBio() != null) doctor.setBio(request.getBio());
        if (request.getIsActive() != null) doctor.setIsActive(request.getIsActive());

        Doctor updated = doctorRepository.save(doctor);
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        return mapToResponse(doctor);
    }

    @Transactional(readOnly = true)
    public PagedResponse<DoctorResponse> searchDoctors(
            String specialization,
            Long clinicId,
            String name,
            Integer minExperience,
            BigDecimal maxFee,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<Doctor> result = doctorRepository.findAll(
                DoctorSpecification.withFilters(specialization, clinicId, name, minExperience, maxFee, true),
                pageRequest
        );

        return new PagedResponse<>(
                result.getContent().stream().map(this::mapToResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }

    @Transactional(readOnly = true)
    public PagedResponse<DoctorResponse> getDoctorsByClinic(Long clinicId, int page, int size) {
        if (!clinicRepository.existsById(clinicId)) {
            throw new ResourceNotFoundException("Clinic not found with id: " + clinicId);
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Doctor> result = doctorRepository.findAll(
                DoctorSpecification.withFilters(null, clinicId, null, null, null, true),
                pageRequest
        );

        return new PagedResponse<>(
                result.getContent().stream().map(this::mapToResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }

    @Transactional
    public void deactivateDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        doctor.setIsActive(false);
        doctorRepository.save(doctor);
    }

    private DoctorResponse mapToResponse(Doctor doctor) {
        User user = doctor.getUser();
        return DoctorResponse.builder()
                .id(doctor.getId())
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhoneNumber())
                .clinicId(doctor.getClinic().getId())
                .clinicName(doctor.getClinic().getName())
                .specialization(doctor.getSpecialization())
                .qualification(doctor.getQualification())
                .registrationNumber(doctor.getRegistrationNumber())
                .experienceYears(doctor.getExperienceYears())
                .consultationFee(doctor.getConsultationFee())
                .bio(doctor.getBio())
                .isActive(doctor.getIsActive())
                .createdAt(doctor.getCreatedAt())
                .build();
    }
}