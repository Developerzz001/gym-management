package com.gymmanagement.billing;

import com.gymmanagement.branch.Branch;
import com.gymmanagement.client.Client;
import com.gymmanagement.common.entity.BaseEntity;
import com.gymmanagement.membership.Membership;
import com.gymmanagement.membership.MembershipDiscount;
import com.gymmanagement.membership.MembershipPlan;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "invoices", indexes = {
        @Index(name = "idx_invoice_client_created", columnList = "client_id,created_at"),
        @Index(name = "idx_invoice_status_due", columnList = "status,due_date"),
        @Index(name = "idx_invoice_branch_created", columnList = "branch_id,created_at")
})
public class Invoice extends BaseEntity {

    @Column(name = "invoice_number", nullable = false, unique = true, length = 40)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_type", nullable = false, length = 30)
    private InvoiceType invoiceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_plan_id")
    private MembershipPlan membershipPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_discount_id")
    private MembershipDiscount membershipDiscount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_membership_id", unique = true)
    private Membership generatedMembership;

    @Column(name = "service_start_date")
    private LocalDate serviceStartDate;

    @Column(name = "session_count")
    private Integer sessionCount;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "description", nullable = false, length = 300)
    private String description;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "tax", nullable = false, precision = 12, scale = 2)
    private BigDecimal tax;

    @Column(name = "discount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discount;

    @Column(name = "final_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalAmount;

    @Column(name = "amount_paid", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountPaid;

    @Column(name = "balance_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InvoiceStatus status;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Version
    private long version;

    @Builder.Default
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("paymentDate DESC, id DESC")
    private List<PaymentTransaction> transactions = new ArrayList<>();
}