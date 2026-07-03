package org.example.weddingcard.dto;

import org.example.weddingcard.entity.GuestbookComment;

import java.time.LocalDateTime;

public record CommentResponse(Long id, String name, String content, LocalDateTime createdAt, boolean hidden) {

    public static CommentResponse from(GuestbookComment comment) {
        return new CommentResponse(comment.getId(), comment.getName(), comment.getContent(), comment.getCreatedAt(), comment.isHidden());
    }
}
