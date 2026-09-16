package com.medibook.clinic.service;

import com.medibook.clinic.dto.*;
import com.medibook.common.dto.PagedResponse;

public interface ClinicService {
    ClinicResponse createClinic(ClinicCreateRequest request, Long creatorUserId);
    ClinicResponse getClinicById(Long clinicId);
    PagedResponse<ClinicResponse> getAllClinics(String city, String search, int page, int size);
    ClinicResponse updateClinic(Long clinicId, ClinicUpdateRequest request);
    void deactivateClinic(Long clinicId);

    StaffResponse assignStaff(Long clinicId, StaffAssignRequest request);
    void removeStaff(Long clinicId, Long staffId);
    java.util.List<StaffResponse> getClinicStaff(Long clinicId);
}