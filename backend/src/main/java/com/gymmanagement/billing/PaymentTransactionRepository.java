package com.gymmanagement.billing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    @Query("select coalesce(sum(case when p.transactionType = com.gymmanagement.billing.TransactionType.PAYMENT " +
	    "then p.paidAmount when p.transactionType = com.gymmanagement.billing.TransactionType.REFUND " +
	    "then -p.paidAmount else 0 end), 0) from PaymentTransaction p")
    BigDecimal netRevenue();

    @Query("select coalesce(sum(case when p.transactionType = com.gymmanagement.billing.TransactionType.PAYMENT " +
	    "then p.paidAmount when p.transactionType = com.gymmanagement.billing.TransactionType.REFUND " +
	    "then -p.paidAmount else 0 end), 0) from PaymentTransaction p where p.paymentDate >= :start")
    BigDecimal netRevenueSince(@Param("start") LocalDateTime start);

	    @Query("select coalesce(sum(case when p.transactionType = com.gymmanagement.billing.TransactionType.PAYMENT " +
		    "then p.paidAmount when p.transactionType = com.gymmanagement.billing.TransactionType.REFUND " +
		    "then -p.paidAmount else 0 end), 0) from PaymentTransaction p " +
			"where p.invoice.branch.id = :branchId and p.paymentDate >= :start and p.paymentDate < :end")
	    BigDecimal netRevenueByBranch(@Param("branchId") Long branchId, @Param("start") LocalDateTime start,
					  @Param("end") LocalDateTime end);

	    @Query("select coalesce(sum(case when p.transactionType = com.gymmanagement.billing.TransactionType.PAYMENT " +
		    "then p.paidAmount when p.transactionType = com.gymmanagement.billing.TransactionType.REFUND " +
		    "then -p.paidAmount else 0 end), 0) from PaymentTransaction p " +
			"where p.invoice.branch.organization.id = :organizationId and p.paymentDate >= :start and p.paymentDate < :end")
	    BigDecimal netRevenueByOrganization(@Param("organizationId") Long organizationId,
						@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}