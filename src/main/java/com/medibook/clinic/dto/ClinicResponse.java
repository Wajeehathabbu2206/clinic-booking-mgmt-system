package com.medibook.clinic.dto;

import com.medibook.clinic.entity.ClinicStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ClinicResponse {
    private Long id;
    private String name;
    private String registrationNumber;
    private String description;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private String phone;
    private String email;
    private ClinicStatus status;
    private Long createdByUserId;
    private LocalDateTime createdAt;
}