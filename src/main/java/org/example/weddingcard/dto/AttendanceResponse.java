package org.example.weddingcard.dto;

import org.example.weddingcard.entity.Attendance;

import java.time.LocalDateTime;

public record AttendanceResponse(Long id, String name, String side, boolean attending, int guestCount, boolean meal,
                                  LocalDateTime createdAt) {

    public static AttendanceResponse from(Attendance attendance) {
        return new AttendanceResponse(attendance.getId(), attendance.getName(), attendance.getSide(),
                attendance.isAttending(), attendance.getGuestCount(), attendance.isMeal(), attendance.getCreatedAt());
    }
}
