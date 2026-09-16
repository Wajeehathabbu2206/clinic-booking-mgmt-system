package com.medibook.clinic.service;

import com.medibook.clinic.dto.*;
import com.medibook.clinic.entity.*;
import com.medibook.clinic.repository.ClinicRepository;
import com.medibook.clinic.repository.ClinicStaffRepository;
import com.medibook.common.dto.PagedResponse;
import com.medibook.common.exception.DuplicateResourceException;
import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.user.entity.User;
import com.medibook.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClinicServiceImpl implements ClinicService {

    private final ClinicRepository clinicRepository;
    private final ClinicStaffRepository clinicStaffRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ClinicResponse createClinic(ClinicCreateRequest request, Long creatorUserId) {
        if (clinicRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new DuplicateResourceException("A clinic with this registration number already exists");
        }

        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + creatorUserId));

        Clinic clinic = new Clinic();
        clinic.setName(request.getName());
        clinic.setRegistrationNumber(request.getRegistrationNumber());
        clinic.setDescription(request.getDescription());
        clinic.setAddressLine1(request.getAddressLine1());
        clinic.setAddressLine2(request.getAddressLine2());
        clinic.setCity(request.getCity());
        clinic.setState(request.getState());
        clinic.setPincode(request.getPincode());
        clinic.setPhone(request.getPhone());
        clinic.setEmail(request.getEmail());
        clinic.setStatus(ClinicStatus.ACTIVE);
        clinic.setCreatedBy(creator);

        Clinic saved = clinicRepository.save(clinic);
        return toResponse(saved);
    }

    @Override
    public ClinicResponse getClinicById(Long clinicId) {
        return toResponse(findClinic(clinicId));
    }

    @Override
    public PagedResponse<ClinicResponse> getAllClinics(String city, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        Page<Clinic> clinicPage;
        if (StringUtils.hasText(search)) {
            clinicPage = clinicRepository.findByNameContainingIgnoreCaseAndStatus(search, ClinicStatus.ACTIVE, pageable);
        } else if (StringUtils.hasText(city)) {
            clinicPage = clinicRepository.findByCityIgnoreCaseAndStatus(city, ClinicStatus.ACTIVE, pageable);
        } else {
            clinicPage = clinicRepository.findByStatus(ClinicStatus.ACTIVE, pageable);
        }

        return PagedResponse.from(clinicPage.map(this::toResponse));
    }

    @Override
    @Transactional
    public ClinicResponse updateClinic(Long clinicId, ClinicUpdateRequest request) {
        Clinic clinic = findClinic(clinicId);

        if (request.getName() != null) clinic.setName(request.getName());
        if (request.getDescription() != null) clinic.setDescription(request.getDescription());
        if (request.getAddressLine1() != null) clinic.setAddressLine1(request.getAddressLine1());
        if (request.getAddressLine2() != null) clinic.setAddressLine2(request.getAddressLine2());
        if (request.getCity() != null) clinic.setCity(request.getCity());
        if (request.getState() != null) clinic.setState(request.getState());
        if (request.getPincode() != null) clinic.setPincode(request.getPincode());
        if (request.getPhone() != null) clinic.setPhone(request.getPhone());
        if (request.getEmail() != null) clinic.setEmail(request.getEmail());

        return toResponse(clinicRepository.save(clinic));
    }

    @Override
    @Transactional
    public void deactivateClinic(Long clinicId) {
        Clinic clinic = findClinic(clinicId);
        clinic.setStatus(ClinicStatus.INACTIVE);
        clinicRepository.save(clinic);
    }

    @Override
    @Transactional
    public StaffResponse assignStaff(Long clinicId, StaffAssignRequest request) {
        Clinic clinic = findClinic(clinicId);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        if (clinicStaffRepository.existsByClinicIdAndUserIdAndStatus(clinicId, user.getId(), StaffStatus.ACTIVE)) {
            throw new DuplicateResourceException("This user is already active staff at this clinic");
        }

        ClinicStaff staff = new ClinicStaff();
        staff.setClinic(clinic);
        staff.setUser(user);
        staff.setRoleInClinic(request.getRoleInClinic());
        staff.setStatus(StaffStatus.ACTIVE);

        ClinicStaff saved = clinicStaffRepository.save(staff);
        return toStaffResponse(saved);
    }

    @Override
    @Transactional
    public void removeStaff(Long clinicId, Long staffId) {
        ClinicStaff staff = clinicStaffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff record not found with id: " + staffId));

        if (!staff.getClinic().getId().equals(clinicId)) {
            throw new ResourceNotFoundException("Staff record not found for this clinic");
        }

        staff.setStatus(StaffStatus.REMOVED);
        clinicStaffRepository.save(staff);
    }

    @Override
    public List<StaffResponse> getClinicStaff(Long clinicId) {
        findClinic(clinicId);
        return clinicStaffRepository.findByClinicIdAndStatus(clinicId, StaffStatus.ACTIVE)
                .stream()
                .map(this::toStaffResponse)
                .toList();
    }

    private Clinic findClinic(Long clinicId) {
        return clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic not found with id: " + clinicId));
    }

    private ClinicResponse toResponse(Clinic clinic) {
        return new ClinicResponse(
                clinic.getId(),
                clinic.getName(),
                clinic.getRegistrationNumber(),
                clinic.getDescription(),
                clinic.getAddressLine1(),
                clinic.getAddressLine2(),
                clinic.getCity(),
                clinic.getState(),
                clinic.getPincode(),
                clinic.getPhone(),
                clinic.getEmail(),
                clinic.getStatus(),
                clinic.getCreatedBy().getId(),
                clinic.getCreatedAt()
        );
    }

    private StaffResponse toStaffResponse(ClinicStaff staff) {
        return new StaffResponse(
                staff.getId(),
                staff.getUser().getId(),
                staff.getUser().getFullName(),
                staff.getUser().getEmail(),
                staff.getRoleInClinic(),
                staff.getStatus()
        );
    }
}