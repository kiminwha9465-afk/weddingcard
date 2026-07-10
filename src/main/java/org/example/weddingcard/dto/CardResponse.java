package org.example.weddingcard.dto;

import java.util.List;
import java.util.Map;

public record CardResponse(String id, Map<String, Object> data, List<CommentResponse> comments, boolean paid) {
}
