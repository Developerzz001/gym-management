package com.gymmanagement.dashboard;

import com.gymmanagement.attendance.AttendanceRepository;
import com.gymmanagement.billing.PaymentTransactionRepository;
import com.gymmanagement.branch.*;
import com.gymmanagement.client.ClientRepository;
import com.gymmanagement.dashboard.dto.*;
import com.gymmanagement.membership.*;
import com.gymmanagement.organization.Organization;
import com.gymmanagement.organization.OrganizationService;
import com.gymmanagement.security.TenantAccessService;
import com.gymmanagement.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MultiBranchDashboardService {
    private final BranchRepository branchRepository;
    private final ClientRepository clientRepository;
    private final MembershipRepository membershipRepository;
    private final PaymentTransactionRepository paymentRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final OrganizationService organizationService;
    private final TenantAccessService tenantAccess;

    public BranchDashboardResponse branch(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new com.gymmanagement.common.exception.ResourceNotFoundException("Branch", "id", branchId));
        tenantAccess.assertBranchAccess(branch);
        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();
        LocalDate monthStart = today.withDayOfMonth(1);
        return new BranchDashboardResponse(branchId, branch.getBranchName(), clientRepository.countByUserBranchId(branchId),
                clientRepository.countByUserBranchIdAndUserActiveTrue(branchId),
                membershipRepository.countByBranchIdAndStartDateBetween(branchId, monthStart, today),
                membershipRepository.countByBranchIdAndEndDateBetweenAndStatus(branchId, today,
                        today.plusDays(30), MembershipStatus.ACTIVE),
                paymentRepository.netRevenueByBranch(branchId, dayStart, dayEnd),
                paymentRepository.netRevenueByBranch(branchId, monthStart.atStartOfDay(), dayEnd),
                attendanceRepository.countByBranchIdAndCheckInAtBetween(branchId, dayStart, dayEnd),
                userRepository.countByBranchIdAndRoleInAndActiveTrue(branchId, List.of(Role.COACH, Role.FITNESS_COACH)),
                userRepository.countByBranchIdAndRoleInAndActiveTrue(branchId, List.of(Role.DIETICIAN)));
    }

    public OrganizationDashboardResponse organization(Long organizationId, LocalDate from, LocalDate to) {
        tenantAccess.assertOrganizationAccess(organizationId);
        Organization organization = organizationService.getEntity(organizationId);
        LocalDate start = from == null ? LocalDate.now().withDayOfMonth(1) : from;
        LocalDate end = to == null ? LocalDate.now() : to;
        List<Branch> branches = branchRepository.findByOrganizationIdAndStatus(organizationId, BranchStatus.ACTIVE);
        List<BranchPerformanceResponse> performance = branches.stream().map(branch -> performance(branch, start, end))
                .sorted(Comparator.comparing(BranchPerformanceResponse::performanceScore).reversed()).toList();
        long members = performance.stream().mapToLong(BranchPerformanceResponse::members).sum();
        BigDecimal revenue = paymentRepository.netRevenueByOrganization(organizationId, start.atStartOfDay(),
                end.plusDays(1).atStartOfDay());
        return new OrganizationDashboardResponse(organizationId, organization.getName(), branches.size(), members,
                revenue, performance);
    }

    private BranchPerformanceResponse performance(Branch branch, LocalDate from, LocalDate to) {
        long members = clientRepository.countByUserBranchId(branch.getId());
        BigDecimal revenue = paymentRepository.netRevenueByBranch(branch.getId(), from.atStartOfDay(), to.plusDays(1).atStartOfDay());
        long attendance = attendanceRepository.countByBranchIdAndCheckInAtBetween(branch.getId(),
                from.atStartOfDay(), to.plusDays(1).atStartOfDay());
        BigDecimal score = BigDecimal.valueOf(members).add(BigDecimal.valueOf(attendance).multiply(BigDecimal.valueOf(0.5)))
                .add(revenue.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP));
        return new BranchPerformanceResponse(branch.getId(), branch.getBranchName(), members, revenue, attendance, score);
    }
}