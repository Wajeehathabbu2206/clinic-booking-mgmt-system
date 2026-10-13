package com.medibook.clinic.service;

import com.medibook.clinic.dto.*;
import com.medibook.clinic.entity.ClinicStatus;
import com.medibook.common.dto.PagedResponse;
import com.medibook.common.util.Role;

public interface ClinicService {
    ClinicResponse createClinic(ClinicCreateRequest request, Long creatorUserId);
    ClinicResponse getClinicById(Long clinicId);

    /**
     * Guests and non-admins always get ACTIVE clinics only.
     * SUPER_ADMIN sees every clinic; clinic managers see clinics they created or manage.
     * Guests and other roles see active clinics only. requesterRole is null for guests.
     */
    PagedResponse<ClinicResponse> getAllClinics(String city, String search, ClinicStatus status,
                                                int page, int size, Long requesterId, Role requesterRole);

    ClinicResponse updateClinic(Long clinicId, ClinicUpdateRequest request, Long requesterId, Role requesterRole);
    void deactivateClinic(Long clinicId);
    void activateClinic(Long clinicId);

    StaffResponse assignStaff(Long clinicId, StaffAssignRequest request, Long requesterId, Role requesterRole);
    void removeStaff(Long clinicId, Long staffId, Long requesterId, Role requesterRole);
    java.util.List<StaffResponse> getClinicStaff(Long clinicId, Long requesterId, Role requesterRole);
}
