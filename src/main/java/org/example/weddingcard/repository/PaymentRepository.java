package org.example.weddingcard.repository;

import org.example.weddingcard.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOwnerEmailOrderByCreatedAtDesc(String ownerEmail);
    boolean existsByCardIdAndStatus(String cardId, Payment.Status status);
}
