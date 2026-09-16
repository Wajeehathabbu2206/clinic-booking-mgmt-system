package com.medibook.user.dto;

import com.medibook.user.entity.Gender;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserProfileUpdateRequest {

    @Size(min = 2, max = 100)
    private String fullName;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit Indian mobile number")
    private String phoneNumber;

    private String profilePictureUrl;

    private Gender gender;

    private LocalDate dateOfBirth;

    private String addressLine1;

    private String city;

    private String state;

    @Pattern(regexp = "^\\d{6}$", message = "Enter a valid 6-digit pincode")
    private String pincode;
}