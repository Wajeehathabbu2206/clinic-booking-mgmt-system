package com.medibook.clinic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClinicUpdateRequest {

    @Size(min = 3, max = 150)
    private String name;

    private String description;

    private String addressLine1;

    private String addressLine2;

    private String city;

    private String state;

    @Pattern(regexp = "^\\d{6}$", message = "Enter a valid 6-digit pincode")
    private String pincode;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit Indian mobile number")
    private String phone;

    @Email
    private String email;
}