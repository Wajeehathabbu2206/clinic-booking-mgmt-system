package com.medibook.analytics.dto;

import com.medibook.appointment.entity.AppointmentStatus;

import java.time.LocalDate;

public record DailyStatusCount(LocalDate date, AppointmentStatus status, long count) {
}