package org.example.weddingcard.entity;

import jakarta.persistence.Entity;
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
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cardId;
    private String name;
    private String side;
    private boolean attending;
    private int guestCount;
    private boolean meal;
    private LocalDateTime createdAt;

    public Attendance(String cardId, String name, String side, boolean attending, int guestCount, boolean meal) {
        this.cardId = cardId;
        this.name = name;
        this.side = side;
        this.attending = attending;
        this.guestCount = guestCount;
        this.meal = meal;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
