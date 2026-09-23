package com.gymmanagement.membership;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership> findByClientIdOrderByStartDateDesc(Long clientId);

    Optional<Membership> findFirstByClientIdOrderByEndDateDesc(Long clientId);

    List<Membership> findByStatus(MembershipStatus status);

    long countByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(MembershipStatus status, LocalDate start, LocalDate end);

    long countByStartDateBetween(LocalDate start, LocalDate end);

    long countByEndDateBetweenAndStatus(LocalDate start, LocalDate end, MembershipStatus status);

    List<Membership> findByStatusAndEndDateIn(MembershipStatus status, List<LocalDate> endDates);

        long countByBranchIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long branchId, MembershipStatus status, LocalDate start, LocalDate end);

        long countByBranchIdAndStartDateBetween(Long branchId, LocalDate start, LocalDate end);

        long countByBranchIdAndEndDateBetweenAndStatus(Long branchId, LocalDate start, LocalDate end,
                                                             MembershipStatus status);
}
