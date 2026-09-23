package com.gymmanagement.branch;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberBranchTransferRepository extends JpaRepository<MemberBranchTransfer, Long> {
    Page<MemberBranchTransfer> findByClientIdOrderByCreatedAtDesc(Long clientId, Pageable pageable);
    Page<MemberBranchTransfer> findByFromBranchIdOrToBranchId(Long fromBranchId, Long toBranchId, Pageable pageable);
}