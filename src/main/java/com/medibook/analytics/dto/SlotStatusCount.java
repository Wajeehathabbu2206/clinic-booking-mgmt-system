package com.medibook.analytics.dto;

import com.medibook.slot.enums.SlotStatus;

public record SlotStatusCount(SlotStatus status, long count) {
}