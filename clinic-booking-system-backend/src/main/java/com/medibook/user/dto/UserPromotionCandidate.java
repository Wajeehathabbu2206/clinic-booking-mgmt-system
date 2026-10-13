package com.medibook.user.dto;

import java.time.LocalDateTime;

public record UserPromotionCandidate(Long id, String fullName, String email, LocalDateTime createdAt) {
}
