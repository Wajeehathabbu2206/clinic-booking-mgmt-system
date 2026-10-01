package com.medibook.clinic.service;

import com.medibook.clinic.entity.Clinic;
import com.medibook.clinic.entity.StaffStatus;
import com.medibook.clinic.repository.ClinicStaffRepository;
import com.medibook.common.exception.ForbiddenException;
import com.medibook.common.util.Role;
import com.medibook.doctor.entity.Doctor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClinicAccessService {

    private final ClinicStaffRepository clinicStaffRepository;

    public boolean isClinicAdmin(Clinic clinic, Long userId, Role role) {
        if (role == Role.SUPER_ADMIN) {
            return true;
        }
        return role == Role.CLINIC_ADMIN
                && (clinic.getCreatedBy().getId().equals(userId)
                || clinicStaffRepository.existsByClinicIdAndUserIdAndStatusAndRoleInClinic(
                        clinic.getId(), userId, StaffStatus.ACTIVE, Role.CLINIC_ADMIN));
    }

    public void assertCanManageClinic(Clinic clinic, Long userId, Role role) {
        if (!isClinicAdmin(clinic, userId, role)) {
            throw new ForbiddenException("You do not have access to manage this clinic");
        }
    }

    public void assertCanManageDoctor(Doctor doctor, Long userId, Role role) {
        if (role == Role.SUPER_ADMIN) {
            return;
        }
        if (role == Role.DOCTOR && doctor.getUser().getId().equals(userId)) {
            return;
        }
        assertCanManageClinic(doctor.getClinic(), userId, role);
    }

    public boolean canViewCalendarPatientDetails(Doctor doctor, Long userId, Role role) {
        if (role == Role.SUPER_ADMIN || (role == Role.DOCTOR && doctor.getUser().getId().equals(userId))) {
            return true;
        }
        if (isClinicAdmin(doctor.getClinic(), userId, role)) {
            return true;
        }
        return role == Role.RECEPTIONIST
                && clinicStaffRepository.existsByClinicIdAndUserIdAndStatusAndRoleInClinic(
                        doctor.getClinic().getId(), userId, StaffStatus.ACTIVE, Role.RECEPTIONIST);
    }

    public void assertCanViewDoctorSchedule(Doctor doctor, Long userId, Role role) {
        if (role == Role.RECEPTIONIST
                && clinicStaffRepository.existsByClinicIdAndUserIdAndStatusAndRoleInClinic(
                        doctor.getClinic().getId(), userId, StaffStatus.ACTIVE, Role.RECEPTIONIST)) {
            return;
        }
        assertCanManageDoctor(doctor, userId, role);
    }
}