package com.gymmanagement.billing;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Invoice i where i.id = :id")
    Optional<Invoice> findByIdForUpdate(@Param("id") Long id);

    Page<Invoice> findByClientId(Long clientId, Pageable pageable);

    Page<Invoice> findByBranchId(Long branchId, Pageable pageable);

    List<Invoice> findByStatusInAndDueDateIn(List<InvoiceStatus> statuses, List<LocalDate> dueDates);

    List<Invoice> findByStatusInAndDueDateBefore(List<InvoiceStatus> statuses, LocalDate date);

    List<Invoice> findByStatusInAndDueDate(List<InvoiceStatus> statuses, LocalDate dueDate);

    long countByStatusIn(List<InvoiceStatus> statuses);

    @Query("select coalesce(sum(i.amountPaid), 0) from Invoice i")
    java.math.BigDecimal totalCollected();

    @Query("select coalesce(sum(i.amountPaid), 0) from Invoice i where i.createdAt >= :start")
    java.math.BigDecimal collectedSince(@Param("start") java.time.LocalDateTime start);

    @Query("select coalesce(sum(i.balanceAmount), 0) from Invoice i where i.status in :statuses")
    java.math.BigDecimal outstanding(@Param("statuses") List<InvoiceStatus> statuses);
}