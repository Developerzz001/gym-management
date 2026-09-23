package com.gymmanagement.attendance;

import com.gymmanagement.attendance.dto.*;
import com.gymmanagement.common.dto.*;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Attendance", description = "Member check-in, check-out and usage reports")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/clients/{clientId}/check-in")
    public ApiResponse<AttendanceResponse> checkIn(@PathVariable Long clientId) {
        return ApiResponse.success("Checked in", attendanceService.checkIn(clientId));
    }

    @PostMapping("/clients/{clientId}/check-out")
    public ApiResponse<AttendanceResponse> checkOut(@PathVariable Long clientId) {
        return ApiResponse.success("Checked out", attendanceService.checkOut(clientId));
    }

    @GetMapping("/clients/{clientId}")
    public ApiResponse<PageResponse<AttendanceResponse>> history(@PathVariable Long clientId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(attendanceService.history(clientId, page, size));
    }

    @GetMapping("/reports")
    public ApiResponse<List<AttendanceResponse>> report(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.success(attendanceService.report(from, to));
    }

    @GetMapping("/reports/clients/{clientId}/usage")
    public ApiResponse<UsageReportResponse> usage(@PathVariable Long clientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.success(attendanceService.usage(clientId, from, to));
    }

    @GetMapping("/reports/peak-hours")
    public ApiResponse<List<PeakHourResponse>> peakHours(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.success(attendanceService.peakHours(from, to));
    }
}