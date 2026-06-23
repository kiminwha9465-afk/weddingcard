package org.example.weddingcard.repository;

import org.example.weddingcard.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByCardIdOrderByCreatedAtDesc(String cardId);
    void deleteByCardId(String cardId);
}
