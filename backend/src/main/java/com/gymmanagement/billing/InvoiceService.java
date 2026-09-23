package com.gymmanagement.billing;

import com.gymmanagement.billing.dto.*;
import com.gymmanagement.client.*;
import com.gymmanagement.common.dto.PageResponse;
import com.gymmanagement.common.exception.*;
import com.gymmanagement.notification.*;
import com.gymmanagement.membership.Membership;
import com.gymmanagement.membership.MembershipDiscount;
import com.gymmanagement.membership.MembershipDiscountService;
import com.gymmanagement.membership.MembershipPlan;
import com.gymmanagement.membership.MembershipPlanService;
import com.gymmanagement.membership.MembershipService;
import com.gymmanagement.security.TenantAccessService;
import com.gymmanagement.user.Role;
import com.gymmanagement.user.User;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceService {

    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private final InvoiceRepository invoiceRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final InvoiceAuditLogRepository auditLogRepository;
    private final ClientRepository clientRepository;
    private final AutomatedNotificationService notificationService;
    private final InvoicePdfService pdfService;
    private final JavaMailSender mailSender;
    private final TenantAccessService tenantAccess;
    private final MembershipPlanService membershipPlanService;
    private final MembershipDiscountService membershipDiscountService;
    private final MembershipService membershipService;

    @Value("${app.billing.invoice-prefix:GYM}")
    private String invoicePrefix;
    @Value("${app.notifications.email-enabled:false}")
    private boolean emailEnabled;
    @Value("${app.notifications.from-email:noreply@gym.local}")
    private String fromEmail;

    @Transactional
    public InvoiceResponse create(CreateInvoiceRequest request) {
        Client client = clientRepository.findById(request.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", request.clientId()));
        assertBranchAccess(client.getUser().getBranch());
        MembershipPlan membershipPlan = resolveMembershipPlan(request);
        MembershipDiscount membershipDiscount = resolveMembershipDiscount(request, membershipPlan);
        validateServiceDetails(request, membershipPlan);
        BigDecimal subtotal = membershipPlan == null ? money(request.totalAmount()) : money(membershipPlan.getFees());
        BigDecimal tax = money(request.tax());
        BigDecimal discount = membershipPlan == null
            ? money(request.discount())
            : membershipDiscount == null ? ZERO
            : money(subtotal.multiply(membershipDiscount.getPercentage()).divide(new BigDecimal("100")));
        BigDecimal finalAmount = subtotal.add(tax).subtract(discount);
        if (finalAmount.compareTo(ZERO) <= 0) {
            throw new BadRequestException("Final amount must be greater than zero");
        }
        Invoice invoice = Invoice.builder()
                .invoiceNumber(nextInvoiceNumber())
                .client(client)
                .branch(client.getUser().getBranch())
                .invoiceType(request.invoiceType())
                .membershipPlan(membershipPlan)
                .membershipDiscount(membershipDiscount)
                .serviceStartDate(request.serviceStartDate())
                .sessionCount(request.sessionCount())
                .purchaseDate(request.purchaseDate())
                .description(request.description().trim())
                .totalAmount(subtotal)
                .tax(tax)
                .discount(discount)
                .finalAmount(finalAmount)
                .amountPaid(ZERO)
                .balanceAmount(finalAmount)
                .status(InvoiceStatus.PENDING)
                .dueDate(request.dueDate())
                .build();
        invoiceRepository.save(invoice);
        audit(invoice, "CREATED", "Invoice created for INR " + finalAmount);
        if (request.initialPaymentAmount() != null && request.initialPaymentAmount().compareTo(ZERO) > 0) {
            if (request.paymentMethod() == null) {
                throw new BadRequestException("Payment method is required for an initial payment");
            }
            applyPayment(invoice, new RecordPaymentRequest(request.initialPaymentAmount(), request.paymentMethod(),
                    request.transactionReference(), request.paymentRemarks()));
        }
        notificationService.send(client.getUser(), NotificationType.INVOICE_CREATED, "New gym invoice",
            "Invoice " + invoice.getInvoiceNumber() + " created. Amount due: INR " + invoice.getBalanceAmount(),
            "INVOICE-" + invoice.getId() + "-CREATED");
        return toResponse(invoiceRepository.save(invoice));
    }

    @Transactional
    public InvoiceResponse recordPayment(Long invoiceId, RecordPaymentRequest request) {
        Invoice invoice = lockedInvoice(invoiceId);
        assertBranchAccess(invoice.getBranch());
        requireCollectible(invoice);
        applyPayment(invoice, request);
        return toResponse(invoiceRepository.save(invoice));
    }

    private void applyPayment(Invoice invoice, RecordPaymentRequest request) {
        BigDecimal amount = money(request.amount());
        if (amount.compareTo(invoice.getBalanceAmount()) > 0) {
            throw new BadRequestException("Payment exceeds outstanding balance of INR " + invoice.getBalanceAmount());
        }
        invoice.setAmountPaid(invoice.getAmountPaid().add(amount));
        invoice.setBalanceAmount(invoice.getFinalAmount().subtract(invoice.getAmountPaid()));
        updateStatus(invoice);
        PaymentStatus paymentStatus = invoice.getStatus() == InvoiceStatus.PAID
                ? PaymentStatus.PAID : PaymentStatus.PARTIALLY_PAID;
        PaymentTransaction transaction = transactionRepository.save(PaymentTransaction.builder()
                .invoice(invoice).paidAmount(amount).transactionType(TransactionType.PAYMENT)
                .status(paymentStatus).paymentMethod(request.paymentMethod())
                .transactionReference(trim(request.transactionReference())).remarks(trim(request.remarks()))
                .paymentDate(LocalDateTime.now()).build());
        invoice.getTransactions().add(transaction);
        activateMembershipIfNeeded(invoice);
        audit(invoice, "PAYMENT_RECEIVED", "Received INR " + amount + " via " + request.paymentMethod());
        notificationService.send(invoice.getClient().getUser(),
                invoice.getStatus() == InvoiceStatus.PAID ? NotificationType.BALANCE_PAYMENT_COMPLETED : NotificationType.PARTIAL_PAYMENT_RECEIVED,
            "Payment received",
                invoice.getStatus() == InvoiceStatus.PAID
                        ? "Invoice " + invoice.getInvoiceNumber() + " is fully paid."
                : "Payment received for " + invoice.getInvoiceNumber() + ". Balance: INR " + invoice.getBalanceAmount(),
            "INVOICE-" + invoice.getId() + "-PAYMENT-" + java.util.UUID.randomUUID());
    }

    @Transactional
    public InvoiceResponse waive(Long invoiceId, AmountRemarksRequest request) {
        Invoice invoice = lockedInvoice(invoiceId);
        requireCollectible(invoice);
        BigDecimal amount = money(request.amount());
        if (amount.compareTo(invoice.getBalanceAmount()) > 0) {
            throw new BadRequestException("Waiver exceeds outstanding balance");
        }
        invoice.setFinalAmount(invoice.getFinalAmount().subtract(amount));
        invoice.setBalanceAmount(invoice.getFinalAmount().subtract(invoice.getAmountPaid()));
        updateStatus(invoice);
        PaymentTransaction transaction = transactionRepository.save(PaymentTransaction.builder()
                .invoice(invoice).paidAmount(amount).transactionType(TransactionType.WAIVER)
                .status(invoice.getStatus() == InvoiceStatus.PAID ? PaymentStatus.PAID : PaymentStatus.PARTIALLY_PAID)
                .remarks(request.remarks().trim()).paymentDate(LocalDateTime.now()).build());
        invoice.getTransactions().add(transaction);
        audit(invoice, "BALANCE_WAIVED", "Waived INR " + amount + ". Reason: " + request.remarks().trim());
        return toResponse(invoiceRepository.save(invoice));
    }

    @Transactional
    public InvoiceResponse refund(Long invoiceId, AmountRemarksRequest request) {
        Invoice invoice = lockedInvoice(invoiceId);
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BadRequestException("Cancelled invoices cannot be refunded");
        }
        BigDecimal amount = money(request.amount());
        if (amount.compareTo(invoice.getAmountPaid()) > 0) {
            throw new BadRequestException("Refund exceeds amount paid");
        }
        invoice.setAmountPaid(invoice.getAmountPaid().subtract(amount));
        invoice.setBalanceAmount(invoice.getFinalAmount().subtract(invoice.getAmountPaid()));
        updateStatus(invoice);
        PaymentTransaction transaction = transactionRepository.save(PaymentTransaction.builder()
                .invoice(invoice).paidAmount(amount).transactionType(TransactionType.REFUND)
                .status(PaymentStatus.REFUNDED).remarks(request.remarks().trim()).paymentDate(LocalDateTime.now()).build());
        invoice.getTransactions().add(transaction);
        audit(invoice, "PAYMENT_REFUNDED", "Refunded INR " + amount + ". Reason: " + request.remarks().trim());
        return toResponse(invoiceRepository.save(invoice));
    }

    public InvoiceResponse get(Long id) {
        Invoice invoice = findInvoice(id);
        assertBranchAccess(invoice.getBranch());
        return toResponse(invoice);
    }

    public PageResponse<InvoiceResponse> list(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        User user = tenantAccess.currentUser();
        if (user.getRole() == Role.ADMIN) {
            return PageResponse.from(invoiceRepository.findAll(pageable).map(this::toResponse));
        }
        if (user.getBranch() == null) {
            throw new org.springframework.security.access.AccessDeniedException("Branch assignment is required");
        }
        return PageResponse.from(invoiceRepository.findByBranchId(user.getBranch().getId(), pageable).map(this::toResponse));
    }

    public PageResponse<InvoiceResponse> listForClient(Long clientId, int page, int size) {
        return PageResponse.from(invoiceRepository.findByClientId(clientId,
                PageRequest.of(page, size, Sort.by("createdAt").descending())).map(this::toResponse));
    }

    public byte[] pdf(Long id) {
        Invoice invoice = findInvoice(id);
        assertBranchAccess(invoice.getBranch());
        return pdfService.generate(invoice);
    }

    public byte[] pdfForClient(Long id, Long clientId) {
        Invoice invoice = findInvoice(id);
        if (!invoice.getClient().getId().equals(clientId)) {
            throw new ResourceNotFoundException("Invoice", "id", id);
        }
        return pdfService.generate(invoice);
    }

    public void shareByEmail(Long id, String requestedEmail) {
        if (!emailEnabled) {
            throw new BadRequestException("Invoice email delivery is disabled");
        }
        Invoice invoice = findInvoice(id);
        String recipient = requestedEmail == null || requestedEmail.isBlank()
                ? invoice.getClient().getUser().getEmail() : requestedEmail;
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(recipient);
            helper.setSubject("Invoice " + invoice.getInvoiceNumber());
            helper.setText("Please find your gym invoice attached. Outstanding balance: INR " + invoice.getBalanceAmount());
            helper.addAttachment(invoice.getInvoiceNumber() + ".pdf", () -> new java.io.ByteArrayInputStream(pdfService.generate(invoice)));
            mailSender.send(message);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not email invoice", exception);
        }
    }

    private Invoice lockedInvoice(Long id) {
        return invoiceRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
    }

    private Invoice findInvoice(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
    }

    private void assertBranchAccess(com.gymmanagement.branch.Branch branch) {
        User user = tenantAccess.currentUser();
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (branch == null) {
            throw new org.springframework.security.access.AccessDeniedException("Branch assignment is required");
        }
        tenantAccess.assertBranchAccess(branch);
    }

    private void requireCollectible(Invoice invoice) {
        if (invoice.getStatus() == InvoiceStatus.PAID || invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BadRequestException("Invoice is not open for collection");
        }
    }

    private void updateStatus(Invoice invoice) {
        if (invoice.getBalanceAmount().compareTo(ZERO) == 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else if (invoice.getAmountPaid().compareTo(ZERO) > 0) {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        } else {
            invoice.setStatus(invoice.getDueDate().isBefore(LocalDate.now()) ? InvoiceStatus.OVERDUE : InvoiceStatus.PENDING);
        }
    }

    private MembershipPlan resolveMembershipPlan(CreateInvoiceRequest request) {
        boolean membershipInvoice = request.invoiceType() == InvoiceType.MEMBERSHIP
                || request.invoiceType() == InvoiceType.MEMBERSHIP_RENEWAL;
        if (!membershipInvoice) {
            if (request.totalAmount() == null) {
                throw new BadRequestException("Total amount is required");
            }
            return null;
        }
        if (request.membershipPlanId() == null) {
            throw new BadRequestException("Membership plan is required");
        }
        return membershipPlanService.getPlanEntityById(request.membershipPlanId());
    }

    private MembershipDiscount resolveMembershipDiscount(CreateInvoiceRequest request, MembershipPlan membershipPlan) {
        if (request.membershipDiscountId() == null) {
            return null;
        }
        if (membershipPlan == null) {
            throw new BadRequestException("Membership discounts can only be used on membership invoices");
        }
        return membershipDiscountService.getActiveEntity(request.membershipDiscountId());
    }

    private void validateServiceDetails(CreateInvoiceRequest request, MembershipPlan membershipPlan) {
        if (membershipPlan != null && request.serviceStartDate() == null) {
            throw new BadRequestException("Membership start date is required");
        }
        if (request.invoiceType() == InvoiceType.PERSONAL_TRAINING
                && (request.serviceStartDate() == null || request.sessionCount() == null)) {
            throw new BadRequestException("Personal training start date and session count are required");
        }
        if (request.invoiceType() == InvoiceType.SUPPLEMENT_PURCHASE && request.purchaseDate() == null) {
            throw new BadRequestException("Supplement purchase date is required");
        }
    }

    private void activateMembershipIfNeeded(Invoice invoice) {
        boolean membershipInvoice = invoice.getInvoiceType() == InvoiceType.MEMBERSHIP
                || invoice.getInvoiceType() == InvoiceType.MEMBERSHIP_RENEWAL;
        if (membershipInvoice && invoice.getMembershipPlan() != null && invoice.getGeneratedMembership() == null) {
            Membership membership = membershipService.activateFromInvoice(invoice);
            invoice.setGeneratedMembership(membership);
        }
    }

    private void audit(Invoice invoice, String action, String details) {
        auditLogRepository.save(InvoiceAuditLog.builder().invoice(invoice).action(action).details(details).build());
    }

    private String nextInvoiceNumber() {
        return invoicePrefix + "-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private BigDecimal money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        List<PaymentTransactionResponse> history = invoice.getTransactions().stream().map(transaction ->
                new PaymentTransactionResponse(transaction.getId(), transaction.getPaidAmount(), transaction.getTransactionType(),
                        transaction.getStatus(), transaction.getPaymentMethod(), transaction.getTransactionReference(),
                        transaction.getRemarks(), transaction.getPaymentDate(), transaction.getCreatedBy())).toList();
        return new InvoiceResponse(invoice.getId(), invoice.getInvoiceNumber(), invoice.getClient().getId(),
            invoice.getClient().getUser().getFullName(), invoice.getInvoiceType(),
            invoice.getMembershipPlan() == null ? null : invoice.getMembershipPlan().getId(),
            invoice.getMembershipPlan() == null ? null : invoice.getMembershipPlan().getName(),
                invoice.getMembershipDiscount() == null ? null : invoice.getMembershipDiscount().getId(),
                invoice.getMembershipDiscount() == null ? null : invoice.getMembershipDiscount().getName(),
                invoice.getMembershipDiscount() == null ? null : invoice.getMembershipDiscount().getPercentage(),
            invoice.getGeneratedMembership() == null ? null : invoice.getGeneratedMembership().getId(),
            invoice.getServiceStartDate(), invoice.getSessionCount(), invoice.getPurchaseDate(), invoice.getDescription(),
                invoice.getTotalAmount(), invoice.getTax(), invoice.getDiscount(), invoice.getFinalAmount(),
                invoice.getAmountPaid(), invoice.getBalanceAmount(), invoice.getStatus(), invoice.getDueDate(),
                invoice.getCreatedAt(), invoice.getCreatedBy(), history);
    }
}