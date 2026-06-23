package org.example.weddingcard.repository;

import org.example.weddingcard.entity.WeddingCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeddingCardRepository extends JpaRepository<WeddingCard, String> {

    List<WeddingCard> findByOwnerEmailOrderByUpdatedAtDesc(String ownerEmail);
}
