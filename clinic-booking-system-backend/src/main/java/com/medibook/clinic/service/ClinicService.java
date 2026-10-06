package com.medibook.clinic.service;

import com.medibook.clinic.dto.*;
import com.medibook.common.dto.PagedResponse;
import com.medibook.common.util.Role;

public interface ClinicService {
    ClinicResponse createClinic(ClinicCreateRequest request, Long creatorUserId);
    ClinicResponse getClinicById(Long clinicId);
    PagedResponse<ClinicResponse> getAllClinics(String city, String search, int page, int size);
    ClinicResponse updateClinic(Long clinicId, ClinicUpdateRequest request, Long requesterId, Role requesterRole);
    void deactivateClinic(Long clinicId);

    StaffResponse assignStaff(Long clinicId, StaffAssignRequest request, Long requesterId, Role requesterRole);
    void removeStaff(Long clinicId, Long staffId, Long requesterId, Role requesterRole);
    java.util.List<StaffResponse> getClinicStaff(Long clinicId, Long requesterId, Role requesterRole);
}