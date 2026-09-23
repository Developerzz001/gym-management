package com.gymmanagement.attendance.dto;

import java.time.LocalDateTime;

public record AttendanceResponse(
        Long id,
        Long clientId,
        String clientName,
        LocalDateTime checkInAt,
        LocalDateTime checkOutAt,
        Long durationMinutes) {
}