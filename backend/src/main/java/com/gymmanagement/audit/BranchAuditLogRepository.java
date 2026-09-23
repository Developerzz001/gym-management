package com.gymmanagement.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchAuditLogRepository extends JpaRepository<BranchAuditLog, Long> {
    Page<BranchAuditLog> findByOrganizationIdAndBranchId(Long organizationId, Long branchId, Pageable pageable);
}