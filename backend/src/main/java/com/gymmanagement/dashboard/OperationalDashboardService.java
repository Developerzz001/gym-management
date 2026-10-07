package com.gymmanagement.dashboard;

import com.gymmanagement.billing.TransactionType;
import com.gymmanagement.branch.Branch;
import com.gymmanagement.branch.BranchRepository;
import com.gymmanagement.client.Client;
import com.gymmanagement.client.FollowUp;
import com.gymmanagement.common.exception.BadRequestException;
import com.gymmanagement.common.exception.ResourceNotFoundException;
import com.gymmanagement.dashboard.dto.CollectionSummaryResponse;
import com.gymmanagement.dashboard.dto.DashboardRecordResponse;
import com.gymmanagement.dashboard.dto.OperationalDashboardSummaryResponse;
import com.gymmanagement.security.TenantAccessService;
import com.gymmanagement.user.Role;
import com.gymmanagement.user.User;
import com.gymmanagement.workout.TrainingSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OperationalDashboardService {

    private final DashboardReadRepository readRepository;
    private final TenantAccessService tenantAccess;
    private final BranchRepository branchRepository;

    @Value("${app.dashboard.renewal-follow-up-days:30}")
    private int renewalFollowUpDays;

    public OperationalDashboardSummaryResponse summary(Long requestedBranchId) {
        User actor = tenantAccess.currentUser();
        LocalDate today = LocalDate.now();
        Long branchId = resolveBranchScope(actor, requestedBranchId);
        Long organizationId = branchId == null ? organizationScope(actor) : null;
        LocalDate renewalStart = today.minusDays(Math.max(1, renewalFollowUpDays));
        return new OperationalDashboardSummaryResponse(
                count(DashboardCategory.MEMBER_BIRTHDAYS, branchId, organizationId, today, renewalStart),
                count(DashboardCategory.STAFF_BIRTHDAYS, branchId, organizationId, today, renewalStart),
                count(DashboardCategory.INQUIRY_FOLLOWUPS, branchId, organizationId, today, renewalStart),
                count(DashboardCategory.RENEWAL_FOLLOWUPS, branchId, organizationId, today, renewalStart),
                count(DashboardCategory.MEMBERSHIP_EXPIRING, branchId, organizationId, today, renewalStart),
                count(DashboardCategory.DONE_FOLLOWUPS, branchId, organizationId, today, renewalStart),
                count(DashboardCategory.BALANCE_PAYMENTS, branchId, organizationId, today, renewalStart),
                count(DashboardCategory.APPOINTMENTS, branchId, organizationId, today, renewalStart),
                collection(branchId, organizationId, today));
    }

    public Page<DashboardRecordResponse> details(DashboardCategory category, String keyword, Long requestedBranchId,
                                                  Pageable pageable) {
        User actor = tenantAccess.currentUser();
        LocalDate today = LocalDate.now();
        Long branchId = resolveBranchScope(actor, requestedBranchId);
        Page<?> page = readRepository.find(category, keyword == null ? "" : keyword,
                branchId, branchId == null ? organizationScope(actor) : null, today,
                today.minusDays(Math.max(1, renewalFollowUpDays)), pageable);
        return page.map(row -> map(category, row, today));
    }

    private long count(DashboardCategory category, Long branchId, Long organizationId,
                       LocalDate today, LocalDate renewalStart) {
        return readRepository.count(category, branchId, organizationId, today, renewalStart);
    }

    private DashboardRecordResponse map(DashboardCategory category, Object row, LocalDate today) {
        if (row instanceof Client client) {
            return new DashboardRecordResponse(client.getId(), client.getUser().getFullName(), client.getUser().getMobileNumber(),
                    activity(client), null, client.getDateOfBirth(), null, null, null, null, null, null, null, null, null);
        }
        if (row instanceof User user) {
            return new DashboardRecordResponse(user.getId(), user.getFullName(), user.getMobileNumber(),
                    null, null, user.getDateOfBirth(), null, null, null, null, null, null, null, null, null);
        }
        if (row instanceof FollowUp followUp) {
            Client client = followUp.getClient();
                return new DashboardRecordResponse(followUp.getId(), client.getUser().getFullName(), client.getUser().getMobileNumber(),
                    activity(client), null, followUp.getFollowUpDate(), null, null, null, null,
                    client.getRegistrationType() == null ? "Follow Up" : client.getRegistrationType().name().replace('_', ' '),
                    followUp.getComment(), followUp.getExecutive() == null ? null : followUp.getExecutive().getFullName(),
                    followUp.getNextFollowUpDate(), client.getId());
        }
        if (row instanceof com.gymmanagement.membership.Membership membership) {
            Client client = membership.getClient();
                return new DashboardRecordResponse(membership.getId(), client.getUser().getFullName(), client.getUser().getMobileNumber(),
                    activity(client), membership.getMembershipPlan().getName(), membership.getEndDate(),
                    ChronoUnit.DAYS.between(today, membership.getEndDate()), null, null, null, null, null, null, null, null);
        }
        if (row instanceof com.gymmanagement.billing.Invoice invoice) {
            Client client = invoice.getClient();
                return new DashboardRecordResponse(invoice.getId(), client.getUser().getFullName(), client.getUser().getMobileNumber(),
                    activity(client), invoice.getMembershipPlan() == null ? null : invoice.getMembershipPlan().getName(),
                    null, null, invoice.getBalanceAmount(), invoice.getDueDate(), null, null, null, null, null, null);
        }
        if (row instanceof TrainingSession session) {
            Client client = session.getClient();
                return new DashboardRecordResponse(session.getId(), client.getUser().getFullName(), client.getUser().getMobileNumber(),
                    activity(client), null, session.getSessionDateTime().toLocalDate(), null, null, null,
                    session.getSessionDateTime(), null, session.getNotes(), null, null, null);
        }
        throw new IllegalArgumentException("Unsupported dashboard row for " + category);
    }

    private CollectionSummaryResponse collection(Long branchId, Long organizationId, LocalDate today) {
        Map<String, BigDecimal> totals = new HashMap<>();
        readRepository.collectionRows(branchId, organizationId, today).forEach(row -> {
            Object method = row[0];
            TransactionType type = (TransactionType) row[1];
            BigDecimal amount = (BigDecimal) row[2];
            if (type == TransactionType.REFUND) amount = amount.negate();
            String key = switch (method == null ? "" : method.toString()) {
                case "CASH" -> "cash";
                case "CREDIT_CARD", "DEBIT_CARD" -> "card";
                case "UPI" -> "upi";
                case "NET_BANKING" -> "bankTransfer";
                case "CHEQUE" -> "cheque";
                case "OTHER" -> "other";
                default -> "other";
            };
            totals.merge(key, amount, BigDecimal::add);
        });
        BigDecimal cash = amount(totals, "cash");
        BigDecimal card = amount(totals, "card");
        BigDecimal upi = amount(totals, "upi");
        BigDecimal bank = amount(totals, "bankTransfer");
        BigDecimal cheque = amount(totals, "cheque");
        BigDecimal other = amount(totals, "other");
        return new CollectionSummaryResponse(cash, card, upi, bank, cheque, other,
                cash.add(card).add(upi).add(bank).add(cheque).add(other));
    }

    private BigDecimal amount(Map<String, BigDecimal> totals, String key) {
        return totals.getOrDefault(key, BigDecimal.ZERO);
    }

    private String activity(Client client) {
        return client.getSportActivity() == null ? null : client.getSportActivity().name();
    }

    private Long resolveBranchScope(User actor, Long requestedBranchId) {
        if (actor.getRole() == Role.ADMIN) {
            if (requestedBranchId == null) throw new BadRequestException("Select a branch to view the operations dashboard");
            if (!branchRepository.existsById(requestedBranchId)) {
                throw new ResourceNotFoundException("Branch", "id", requestedBranchId);
            }
            return requestedBranchId;
        }
        if (actor.getRole() == Role.SUPER_ADMIN || actor.getRole() == Role.ORGANIZATION_ADMIN) {
            if (requestedBranchId == null) throw new BadRequestException("Select a branch to view the operations dashboard");
            Branch branch = branchRepository.findById(requestedBranchId)
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", requestedBranchId));
            tenantAccess.assertBranchAccess(branch);
            return branch.getId();
        }
        if (actor.getBranch() == null) return -1L;
        if (requestedBranchId != null && !actor.getBranch().getId().equals(requestedBranchId)) {
            Branch requestedBranch = branchRepository.findById(requestedBranchId)
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", requestedBranchId));
            tenantAccess.assertBranchAccess(requestedBranch);
        }
        return actor.getBranch().getId();
    }

    private Long organizationScope(User actor) {
        if (actor.getRole() == Role.SUPER_ADMIN || actor.getRole() == Role.ADMIN) return null;
        return actor.getOrganization() == null ? -1L : actor.getOrganization().getId();
    }
}