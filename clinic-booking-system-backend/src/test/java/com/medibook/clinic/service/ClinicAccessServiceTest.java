package com.medibook.clinic.service;

import com.medibook.clinic.entity.Clinic;
import com.medibook.clinic.repository.ClinicStaffRepository;
import com.medibook.common.exception.ForbiddenException;
import com.medibook.common.util.Role;
import com.medibook.user.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClinicAccessServiceTest {

    @Test
    void deniesAdminWhoIsNotOwnerOrActiveClinicAdmin() {
        ClinicStaffRepository staffRepository = mock(ClinicStaffRepository.class);
        ClinicAccessService accessService = new ClinicAccessService(staffRepository);
        User creator = new User();
        creator.setId(10L);
        Clinic clinic = new Clinic();
        clinic.setId(100L);
        clinic.setCreatedBy(creator);
        when(staffRepository.existsByClinicIdAndUserIdAndStatusAndRoleInClinic(
                100L, 20L, com.medibook.clinic.entity.StaffStatus.ACTIVE, Role.CLINIC_ADMIN))
                .thenReturn(false);

        assertThrows(ForbiddenException.class,
                () -> accessService.assertCanManageClinic(clinic, 20L, Role.CLINIC_ADMIN));
    }
}