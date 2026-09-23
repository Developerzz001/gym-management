package com.gymmanagement.attendance.dto;

public record UsageReportResponse(Long clientId, long visits, long totalMinutes, double averageMinutes) {
}