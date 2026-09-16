package com.medibook.clinic.dto;

import com.medibook.clinic.entity.StaffStatus;
import com.medibook.common.util.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StaffResponse {
    private Long staffId;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private Role roleInClinic;
    private StaffStatus status;
}