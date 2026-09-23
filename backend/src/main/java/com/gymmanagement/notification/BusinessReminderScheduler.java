package com.gymmanagement.notification;

import com.gymmanagement.billing.*;
import com.gymmanagement.membership.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BusinessReminderScheduler {

    private static final List<InvoiceStatus> OPEN = List.of(InvoiceStatus.PENDING, InvoiceStatus.PARTIALLY_PAID);
    private final InvoiceRepository invoiceRepository;
    private final MembershipRepository membershipRepository;
    private final AutomatedNotificationService notifications;

    @Scheduled(cron = "${app.notifications.billing-reminder-cron:0 0 9 * * *}", zone = "${app.notifications.time-zone:Asia/Kolkata}")
    @Transactional
    public void sendBillingReminders() {
        LocalDate today = LocalDate.now();
        for (int days : List.of(3, 1)) {
            for (Invoice invoice : invoiceRepository.findByStatusInAndDueDate(OPEN, today.plusDays(days))) {
                sendInvoiceReminder(invoice, "DUE_" + days,
                        "Payment reminder: invoice " + invoice.getInvoiceNumber() + " is due in " + days
                                + " day(s). Outstanding: INR " + invoice.getBalanceAmount());
            }
        }
        for (Invoice invoice : invoiceRepository.findByStatusInAndDueDateBefore(OPEN, today)) {
            invoice.setStatus(InvoiceStatus.OVERDUE);
            sendInvoiceReminder(invoice, "OVERDUE_" + today,
                    "Invoice " + invoice.getInvoiceNumber() + " is overdue. Outstanding: INR " + invoice.getBalanceAmount());
        }
        log.info("Billing reminder job completed for {}", today);
    }

    @Scheduled(cron = "${app.notifications.membership-expiry-cron:0 15 9 * * *}", zone = "${app.notifications.time-zone:Asia/Kolkata}")
    @Transactional(readOnly = true)
    public void sendMembershipExpiryReminders() {
        LocalDate today = LocalDate.now();
        List<Integer> reminderDays = List.of(30, 15, 7, 1);
        List<LocalDate> dates = reminderDays.stream().map(today::plusDays).toList();
        for (Membership membership : membershipRepository.findByStatusAndEndDateIn(MembershipStatus.ACTIVE, dates)) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(today, membership.getEndDate());
            String message = "Your membership expires in " + days + " day(s), on " + membership.getEndDate() + ".";
            notifications.send(membership.getClient().getUser(), NotificationType.MEMBERSHIP_EXPIRY,
                    "Membership expiry reminder", message, "MEMBERSHIP-" + membership.getId() + "-EXPIRY-" + days);
        }
        log.info("Membership expiry reminder job completed for {}", today);
    }

    private void sendInvoiceReminder(Invoice invoice, String event, String message) {
        notifications.send(invoice.getClient().getUser(), NotificationType.PAYMENT_OVERDUE,
                "Payment reminder", message, "INVOICE-" + invoice.getId() + "-" + event);
    }
}