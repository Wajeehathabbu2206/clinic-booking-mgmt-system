package com.medibook.clinic.dto;

import com.medibook.common.util.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffAssignRequest {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotNull(message = "Role in clinic is required")
    private Role roleInClinic;
}