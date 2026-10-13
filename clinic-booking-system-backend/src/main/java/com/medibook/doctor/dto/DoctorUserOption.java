package com.medibook.doctor.dto;

import com.medibook.common.util.Role;

public record DoctorUserOption(Long userId, String fullName, String email, Role role) {
}
