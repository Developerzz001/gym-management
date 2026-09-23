package com.gymmanagement.dashboard;

import com.gymmanagement.attendance.*;
import com.gymmanagement.attendance.dto.PeakHourResponse;
import com.gymmanagement.billing.*;
import com.gymmanagement.dashboard.dto.*;
import com.gymmanagement.diet.DietPlanRepository;
import com.gymmanagement.membership.*;
import com.gymmanagement.workout.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.*;
import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final List<InvoiceStatus> OPEN = List.of(InvoiceStatus.PENDING, InvoiceStatus.PARTIALLY_PAID, InvoiceStatus.OVERDUE);
    private final InvoiceRepository invoiceRepository;
        private final PaymentTransactionRepository transactionRepository;
    private final MembershipRepository membershipRepository;
    private final AttendanceRepository attendanceRepository;
    private final TrainingSessionRepository sessionRepository;
    private final DietPlanRepository dietPlanRepository;

    public AdminDashboardResponse adminDashboard() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDateTime monthStartTime = monthStart.atStartOfDay();

        BigDecimal totalRevenue = transactionRepository.netRevenue();
        BigDecimal monthlyRevenue = transactionRepository.netRevenueSince(monthStartTime);
        BigDecimal todayRevenue = transactionRepository.netRevenueSince(todayStart);
        BigDecimal outstanding = invoiceRepository.outstanding(OPEN);
        BigDecimal overdue = invoiceRepository.outstanding(List.of(InvoiceStatus.OVERDUE));
        BigDecimal billed = totalRevenue.add(outstanding);
        BigDecimal efficiency = billed.signum() == 0 ? BigDecimal.ZERO
                : totalRevenue.multiply(BigDecimal.valueOf(100)).divide(billed, 2, RoundingMode.HALF_UP);

        List<PeakHourResponse> peaks = attendanceRepository.peakHours(monthStartTime, tomorrowStart, PageRequest.of(0, 3))
                .stream().map(row -> new PeakHourResponse(((Number) row[0]).intValue(), ((Number) row[1]).longValue())).toList();
        List<PerformanceResponse> coaches = sessionRepository.topCoaches(monthStartTime, tomorrowStart, PageRequest.of(0, 5))
                .stream().map(this::performance).toList();
        List<PerformanceResponse> dieticians = dietPlanRepository.topDieticians(monthStartTime, tomorrowStart, PageRequest.of(0, 5))
                .stream().map(this::performance).toList();

        return new AdminDashboardResponse(totalRevenue, monthlyRevenue, todayRevenue, outstanding, overdue, efficiency,
                membershipRepository.countByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(MembershipStatus.ACTIVE, today, today),
                membershipRepository.countByStartDateBetween(monthStart, today),
                membershipRepository.countByStartDateBetween(monthStart, today),
                membershipRepository.countByEndDateBetweenAndStatus(today, today.plusDays(30), MembershipStatus.ACTIVE),
                attendanceRepository.countByCheckInAtBetween(todayStart, tomorrowStart),
                attendanceRepository.countByCheckInAtBetween(monthStartTime, tomorrowStart), peaks,
                sessionRepository.countByStatusAndSessionDateTimeBetween(SessionStatus.COMPLETED, monthStartTime, tomorrowStart),
                coaches, dieticians);
    }

    private PerformanceResponse performance(Object[] row) {
        return new PerformanceResponse(((Number) row[0]).longValue(), row[1] + " " + row[2], ((Number) row[3]).longValue());
    }
}