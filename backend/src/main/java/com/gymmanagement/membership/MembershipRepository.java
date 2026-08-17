package com.gymmanagement.membership;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership> findByClientIdOrderByStartDateDesc(Long clientId);

    Optional<Membership> findFirstByClientIdOrderByEndDateDesc(Long clientId);

    List<Membership> findByStatus(MembershipStatus status);
}
