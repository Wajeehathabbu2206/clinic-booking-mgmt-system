package com.medibook.clinic.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClinicCreateRequest {

    @NotBlank(message = "Clinic name is required")
    @Size(min = 3, max = 150)
    private String name;

    @NotBlank(message = "Registration number is required")
    private String registrationNumber;

    private String description;

    @NotBlank(message = "Address is required")
    private String addressLine1;

    private String addressLine2;

    @NotBlank
    private String city;

    @NotBlank
    private String state;

    @NotBlank
    @Pattern(regexp = "^\\d{6}$", message = "Enter a valid 6-digit pincode")
    private String pincode;

    @NotBlank
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit Indian mobile number")
    private String phone;

    @Email
    private String email;
}