package com.medibook.user.dto;

import com.medibook.common.util.Role;
import com.medibook.user.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String profilePictureUrl;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String addressLine1;
    private String city;
    private String state;
    private String pincode;
    private Role role;
    private boolean enabled;
    private LocalDateTime createdAt;
}