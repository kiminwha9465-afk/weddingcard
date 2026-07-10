package org.example.weddingcard.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Payment {

    public enum Status { DONE, CANCELED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cardId;
    private String ownerEmail;
    private String orderId;
    private String paymentKey;
    private int amount;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Payment(String cardId, String ownerEmail, String orderId,
                   String paymentKey, int amount, LocalDateTime paidAt) {
        this.cardId = cardId;
        this.ownerEmail = ownerEmail;
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.status = Status.DONE;
        this.paidAt = paidAt;
    }
}
