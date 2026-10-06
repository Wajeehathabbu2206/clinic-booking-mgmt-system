package com.medibook.analytics.dto;

import com.medibook.appointment.entity.AppointmentStatus;

public record StatusCount(AppointmentStatus status, long count) {
}