package org.example.weddingcard.dto;

import java.time.LocalDateTime;

public record CardSummaryResponse(String id, String names, String dateText, String adminKey, LocalDateTime updatedAt, boolean paid) {
}
