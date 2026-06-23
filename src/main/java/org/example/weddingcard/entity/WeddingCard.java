package org.example.weddingcard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class WeddingCard implements Persistable<String> {

    @Id
    private String id;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String dataJson;

    private String adminKey;

    private String ownerEmail;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WeddingCard(String id, String dataJson) {
        this.id = id;
        this.dataJson = dataJson;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        adminKey = UUID.randomUUID().toString().replace("-", "");
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean isNew() {
        return createdAt == null;
    }
}
