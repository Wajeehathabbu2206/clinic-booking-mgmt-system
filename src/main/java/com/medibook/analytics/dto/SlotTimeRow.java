package com.medibook.analytics.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record SlotTimeRow(LocalDate slotDate, LocalTime startTime) {
}