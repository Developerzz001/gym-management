package com.gymmanagement.billing;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceAuditLogRepository extends JpaRepository<InvoiceAuditLog, Long> {
}