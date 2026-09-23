package com.gymmanagement.billing;

import com.gymmanagement.billing.dto.*;
import com.gymmanagement.client.*;
import com.gymmanagement.common.exception.BadRequestException;
import com.gymmanagement.notification.AutomatedNotificationService;
import com.gymmanagement.membership.*;
import com.gymmanagement.security.TenantAccessService;
import com.gymmanagement.user.Role;
import com.gymmanagement.user.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock InvoiceRepository invoiceRepository;
    @Mock PaymentTransactionRepository transactionRepository;
    @Mock InvoiceAuditLogRepository auditLogRepository;
    @Mock ClientRepository clientRepository;
    @Mock AutomatedNotificationService notificationService;
    @Mock InvoicePdfService pdfService;
    @Mock JavaMailSender mailSender;
    @Mock TenantAccessService tenantAccess;
    @Mock MembershipPlanService membershipPlanService;
    @Mock MembershipDiscountService membershipDiscountService;
    @Mock MembershipService membershipService;
    @InjectMocks InvoiceService service;

    private Invoice invoice;

    @BeforeEach
    void setUp() {
        User user = User.builder().firstName("Asha").lastName("Patel").email("asha@example.com").password("x").build();
        user.setId(10L);
        Client client = Client.builder().user(user).build();
        client.setId(20L);
        invoice = Invoice.builder().invoiceNumber("GYM-1").client(client).invoiceType(InvoiceType.MEMBERSHIP)
                .description("Membership").totalAmount(new BigDecimal("3000.00")).tax(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO).finalAmount(new BigDecimal("3000.00")).amountPaid(BigDecimal.ZERO)
                .balanceAmount(new BigDecimal("3000.00")).status(InvoiceStatus.PENDING)
                .dueDate(LocalDate.now().plusDays(7)).transactions(new ArrayList<>()).build();
        invoice.setId(1L);
        lenient().when(invoiceRepository.findByIdForUpdate(1L)).thenReturn(java.util.Optional.of(invoice));
        when(tenantAccess.currentUser()).thenReturn(User.builder().role(Role.ADMIN).build());
    }

    @Test
    void partialThenBalancePaymentClosesInvoiceAndKeepsHistory() {
        when(invoiceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(invocation -> {
            PaymentTransaction transaction = invocation.getArgument(0);
            transaction.setId(100L);
            return transaction;
        });
        InvoiceResponse partial = service.recordPayment(1L,
                new RecordPaymentRequest(new BigDecimal("2000"), PaymentMethod.UPI, "UPI-1", null));
        assertEquals(InvoiceStatus.PARTIALLY_PAID, partial.status());
        assertEquals(new BigDecimal("1000.00"), partial.balanceAmount());

        InvoiceResponse settled = service.recordPayment(1L,
                new RecordPaymentRequest(new BigDecimal("1000"), PaymentMethod.CASH, null, "Balance"));
        assertEquals(InvoiceStatus.PAID, settled.status());
        assertEquals(new BigDecimal("0.00"), settled.balanceAmount());
        assertEquals(2, settled.paymentHistory().size());
    }

    @Test
    void rejectsOverpaymentWithoutCreatingTransaction() {
        assertThrows(BadRequestException.class, () -> service.recordPayment(1L,
                new RecordPaymentRequest(new BigDecimal("3000.01"), PaymentMethod.CASH, null, null)));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void fullInitialPaymentCreatesPaidInvoice() {
        when(clientRepository.findById(20L)).thenReturn(java.util.Optional.of(invoice.getClient()));
        when(invoiceRepository.save(any())).thenAnswer(invocation -> {
            Invoice saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        InvoiceResponse response = service.create(new CreateInvoiceRequest(20L, InvoiceType.SUPPLEMENT_PURCHASE,
                "Supplements", new BigDecimal("1000.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                LocalDate.now().plusDays(7), null, null, null, null, LocalDate.now(),
                new BigDecimal("1000.00"), PaymentMethod.UPI, "UPI-1", null));

        assertEquals(InvoiceStatus.PAID, response.status());
        assertEquals(new BigDecimal("1000.00"), response.amountPaid());
        assertEquals(new BigDecimal("0.00"), response.balanceAmount());
        assertEquals(1, response.paymentHistory().size());
        verify(transactionRepository).save(any(PaymentTransaction.class));
    }

    @Test
    void membershipInvoiceUsesPlanPriceAndDiscount() {
        MembershipPlan plan = MembershipPlan.builder().name("Monthly").durationDays(30)
            .fees(new BigDecimal("2000.00")).extraDurationDays(5).build();
        plan.setId(5L);
        MembershipDiscount discount = MembershipDiscount.builder().name("Launch Offer")
            .percentage(new BigDecimal("10.00")).active(true).build();
        discount.setId(6L);
        when(clientRepository.findById(20L)).thenReturn(java.util.Optional.of(invoice.getClient()));
        when(membershipPlanService.getPlanEntityById(5L)).thenReturn(plan);
        when(membershipDiscountService.getActiveEntity(6L)).thenReturn(discount);
        when(invoiceRepository.save(any())).thenAnswer(invocation -> {
            Invoice saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        InvoiceResponse response = service.create(new CreateInvoiceRequest(20L, InvoiceType.MEMBERSHIP,
                "Monthly membership", null, BigDecimal.ZERO, BigDecimal.ZERO, LocalDate.now().plusDays(7),
            5L, 6L, LocalDate.now(), null, null, null, null, null, null));

        assertEquals(new BigDecimal("2000.00"), response.totalAmount());
        assertEquals(new BigDecimal("200.00"), response.discount());
        assertEquals(new BigDecimal("1800.00"), response.finalAmount());
        assertEquals(5L, response.membershipPlanId());
        assertEquals(6L, response.membershipDiscountId());
        assertEquals(new BigDecimal("10.00"), response.membershipDiscountPercentage());
    }

    @Test
    void firstPaymentCreatesMembershipOnlyOnce() {
        MembershipPlan plan = MembershipPlan.builder().name("Monthly").durationDays(30)
            .fees(new BigDecimal("3000.00")).extraDurationDays(0).build();
        plan.setId(5L);
        invoice.setMembershipPlan(plan);
        invoice.setServiceStartDate(LocalDate.now());
        Membership membership = Membership.builder().build();
        membership.setId(99L);
        when(membershipService.activateFromInvoice(invoice)).thenReturn(membership);
        when(invoiceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.recordPayment(1L, new RecordPaymentRequest(new BigDecimal("1000"), PaymentMethod.CASH, null, null));
        service.recordPayment(1L, new RecordPaymentRequest(new BigDecimal("1000"), PaymentMethod.CASH, null, null));

        verify(membershipService, times(1)).activateFromInvoice(invoice);
        assertEquals(99L, invoice.getGeneratedMembership().getId());
    }
}