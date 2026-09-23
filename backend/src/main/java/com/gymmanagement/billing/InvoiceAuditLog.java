package com.gymmanagement.billing;

import com.gymmanagement.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "invoice_audit_logs", indexes = @Index(name = "idx_invoice_audit_invoice", columnList = "invoice_id,created_at"))
public class InvoiceAuditLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "action", nullable = false, length = 40)
    private String action;

    @Column(name = "details", nullable = false, length = 1000)
    private String details;
}