package org.example.weddingcard.dto;

import java.time.LocalDateTime;

public record AdminCardResponse(String id, String ownerEmail, String names, String dateText,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
}
