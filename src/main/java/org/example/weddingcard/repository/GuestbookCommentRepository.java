package org.example.weddingcard.repository;

import org.example.weddingcard.entity.GuestbookComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuestbookCommentRepository extends JpaRepository<GuestbookComment, Long> {
    List<GuestbookComment> findByCardIdOrderByCreatedAtDesc(String cardId);
    void deleteByCardId(String cardId);
}
