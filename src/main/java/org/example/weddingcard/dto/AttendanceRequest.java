package org.example.weddingcard.dto;

public record AttendanceRequest(String name, String side, boolean attending, int guestCount, boolean meal) {
}
