package com.gymmanagement.billing;

import com.gymmanagement.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "payment_transactions", indexes = {
        @Index(name = "idx_payment_invoice_date", columnList = "invoice_id,payment_date"),
        @Index(name = "idx_payment_reference", columnList = "transaction_reference")
})
public class PaymentTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "paid_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal paidAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "transaction_reference", length = 120)
    private String transactionReference;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;
}