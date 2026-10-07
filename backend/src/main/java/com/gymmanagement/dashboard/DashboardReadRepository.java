package com.gymmanagement.dashboard;

import com.gymmanagement.user.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class DashboardReadRepository {

    private final EntityManager entityManager;

    public Page<?> find(DashboardCategory category, String keyword, Long branchId, Long organizationId,
                        LocalDate today, LocalDate renewalStart, Pageable pageable) {
        QueryParts parts = parts(category, keyword, branchId, organizationId, today, renewalStart);
        List<?> rows = bind(entityManager.createQuery(parts.select() + parts.where() + sort(category, pageable)), parts.params())
                .setFirstResult((int) pageable.getOffset()).setMaxResults(pageable.getPageSize()).getResultList();
        long count = ((Number) bind(entityManager.createQuery(parts.count()), parts.params()).getSingleResult()).longValue();
        return new PageImpl<>(rows, pageable, count);
    }

    public long count(DashboardCategory category, Long branchId, Long organizationId, LocalDate today,
                      LocalDate renewalStart) {
        QueryParts parts = parts(category, "", branchId, organizationId, today, renewalStart);
        return ((Number) bind(entityManager.createQuery(parts.count()), parts.params()).getSingleResult()).longValue();
    }

    public List<Object[]> collectionRows(Long branchId, Long organizationId, LocalDate today) {
        StringBuilder jpql = new StringBuilder("select p.paymentMethod, p.transactionType, sum(p.paidAmount) from PaymentTransaction p "
                + "where p.paymentDate >= :start and p.paymentDate < :end and p.transactionType in "
                + "(com.gymmanagement.billing.TransactionType.PAYMENT, com.gymmanagement.billing.TransactionType.REFUND) "
                + "and p.status in (com.gymmanagement.billing.PaymentStatus.PAID, "
                + "com.gymmanagement.billing.PaymentStatus.PARTIALLY_PAID, com.gymmanagement.billing.PaymentStatus.REFUNDED)");
        Map<String, Object> params = new HashMap<>();
        params.put("start", today.atStartOfDay());
        params.put("end", today.plusDays(1).atStartOfDay());
        if (branchId != null) {
            jpql.append(" and p.invoice.branch.id = :branchId");
            params.put("branchId", branchId);
        }
        if (organizationId != null) {
            jpql.append(" and p.invoice.branch.organization.id = :organizationId");
            params.put("organizationId", organizationId);
        }
        jpql.append(" group by p.paymentMethod, p.transactionType");
        return bind(entityManager.createQuery(jpql.toString()), params).getResultList();
    }

    private QueryParts parts(DashboardCategory category, String keyword, Long branchId, Long organizationId,
                             LocalDate today, LocalDate renewalStart) {
        String select;
        String alias;
        String scope;
        String condition;
        Map<String, Object> params = new HashMap<>();
        params.put("branchId", branchId);
        params.put("organizationId", organizationId);
        if (!keyword.isBlank()) params.put("keyword", "%" + keyword.trim().toLowerCase() + "%");
        switch (category) {
            case MEMBER_BIRTHDAYS -> {
                select = "select c from Client c"; alias = "c"; scope = "c.user";
                condition = "c.dateOfBirth is not null and extract(month from c.dateOfBirth) = :month "
                    + "and extract(day from c.dateOfBirth) = :day and (c.registrationType is null "
                    + "or c.registrationType = com.gymmanagement.client.RegistrationType.REGISTERED)";
                params.put("month", today.getMonthValue()); params.put("day", today.getDayOfMonth());
            }
            case STAFF_BIRTHDAYS -> {
                select = "select u from User u"; alias = "u"; scope = "u";
                condition = "u.active = true and u.dateOfBirth is not null and extract(month from u.dateOfBirth) = :month "
                    + "and extract(day from u.dateOfBirth) = :day and u.role in "
                        + "(com.gymmanagement.user.Role.BRANCH_MANAGER, com.gymmanagement.user.Role.COACH, "
                        + "com.gymmanagement.user.Role.FITNESS_COACH, com.gymmanagement.user.Role.DIETICIAN, com.gymmanagement.user.Role.RECEPTIONIST)";
                params.put("month", today.getMonthValue()); params.put("day", today.getDayOfMonth());
            }
            case INQUIRY_FOLLOWUPS -> {
                select = "select f from FollowUp f"; alias = "f"; scope = "f.client.user";
                condition = "f.nextFollowUpDate = :today and f.client.registrationType = com.gymmanagement.client.RegistrationType.INQUIRY";
                params.put("today", today);
            }
            case RENEWAL_FOLLOWUPS -> {
                select = "select m from Membership m"; alias = "m"; scope = "m.client.user";
                condition = "m.endDate < :today and m.endDate >= :renewalStart";
                params.put("today", today); params.put("renewalStart", renewalStart);
            }
            case MEMBERSHIP_EXPIRING -> {
                select = "select m from Membership m"; alias = "m"; scope = "m.client.user";
                condition = "m.status = com.gymmanagement.membership.MembershipStatus.ACTIVE and m.endDate between :today and :weekEnd";
                params.put("today", today); params.put("weekEnd", today.plusDays(7));
            }
            case DONE_FOLLOWUPS -> {
                select = "select f from FollowUp f"; alias = "f"; scope = "f.client.user";
                condition = "f.followUpDate = :today"; params.put("today", today);
            }
            case BALANCE_PAYMENTS -> {
                select = "select i from Invoice i"; alias = "i"; scope = "i.client.user";
                condition = "i.balanceAmount > 0 and i.status in "
                        + "(com.gymmanagement.billing.InvoiceStatus.PENDING, com.gymmanagement.billing.InvoiceStatus.PARTIALLY_PAID, "
                        + "com.gymmanagement.billing.InvoiceStatus.OVERDUE)";
            }
            case APPOINTMENTS -> {
                select = "select s from TrainingSession s"; alias = "s"; scope = "s.client.user";
                condition = "s.status = com.gymmanagement.workout.SessionStatus.SCHEDULED and s.sessionDateTime >= :start "
                        + "and s.sessionDateTime < :end";
                params.put("start", today.atStartOfDay()); params.put("end", today.plusDays(1).atStartOfDay());
            }
            default -> throw new IllegalArgumentException("Unsupported dashboard category");
        }
        String user = category == DashboardCategory.STAFF_BIRTHDAYS ? alias : scope;
        String search = " and (lower(concat(" + user + ".firstName, ' ', " + user + ".lastName)) like :keyword "
                + "or lower(coalesce(" + user + ".mobileNumber, '')) like :keyword)";
        String scoping = " and (:branchId is null or " + scope + ".branch.id = :branchId)"
                + " and (:organizationId is null or " + scope + ".organization.id = :organizationId)";
        String where = " where " + condition + scoping + (keyword.isBlank() ? "" : search);
        String entity = switch (category) {
            case MEMBER_BIRTHDAYS, INQUIRY_FOLLOWUPS, DONE_FOLLOWUPS -> category == DashboardCategory.MEMBER_BIRTHDAYS ? "Client" : "FollowUp";
            case STAFF_BIRTHDAYS -> "User";
            case RENEWAL_FOLLOWUPS, MEMBERSHIP_EXPIRING -> "Membership";
            case BALANCE_PAYMENTS -> "Invoice";
            case APPOINTMENTS -> "TrainingSession";
        };
        return new QueryParts(select, "select count(" + alias + ".id) from " + entity + " " + alias + where, where, params);
    }

    private String sort(DashboardCategory category, Pageable pageable) {
        String property = pageable.getSort().stream().findFirst().map(order -> order.getProperty()).orElse("date");
        String path = switch (property) {
            case "name" -> switch (category) {
                case STAFF_BIRTHDAYS -> "u.firstName";
                case INQUIRY_FOLLOWUPS, DONE_FOLLOWUPS -> "f.client.user.firstName";
                case RENEWAL_FOLLOWUPS, MEMBERSHIP_EXPIRING -> "m.client.user.firstName";
                case BALANCE_PAYMENTS -> "i.client.user.firstName";
                case APPOINTMENTS -> "s.client.user.firstName";
                default -> "c.user.firstName";
            };
            case "mobileNumber" -> switch (category) {
                case STAFF_BIRTHDAYS -> "u.mobileNumber";
                case INQUIRY_FOLLOWUPS, DONE_FOLLOWUPS -> "f.client.user.mobileNumber";
                case RENEWAL_FOLLOWUPS, MEMBERSHIP_EXPIRING -> "m.client.user.mobileNumber";
                case BALANCE_PAYMENTS -> "i.client.user.mobileNumber";
                case APPOINTMENTS -> "s.client.user.mobileNumber";
                default -> "c.user.mobileNumber";
            };
            case "pendingAmount" -> category == DashboardCategory.BALANCE_PAYMENTS ? "i.balanceAmount" : "i.dueDate";
            case "dueDate" -> category == DashboardCategory.BALANCE_PAYMENTS ? "i.dueDate" : "i.client.user.firstName";
            case "appointmentTime" -> category == DashboardCategory.APPOINTMENTS ? "s.sessionDateTime" : "s.client.user.firstName";
            default -> switch (category) {
                case INQUIRY_FOLLOWUPS, DONE_FOLLOWUPS -> "f.followUpDate";
                case RENEWAL_FOLLOWUPS, MEMBERSHIP_EXPIRING -> "m.endDate";
                case BALANCE_PAYMENTS -> "i.dueDate";
                case APPOINTMENTS -> "s.sessionDateTime";
                case STAFF_BIRTHDAYS -> "u.firstName";
                default -> "c.user.firstName";
            };
        };
        boolean ascending = pageable.getSort().stream().findFirst().map(order -> order.isAscending()).orElse(true);
        return " order by " + path + (ascending ? " asc" : " desc");
    }

    private Query bind(Query query, Map<String, Object> params) {
        params.forEach(query::setParameter);
        return query;
    }

    private record QueryParts(String select, String count, String where, Map<String, Object> params) { }
}