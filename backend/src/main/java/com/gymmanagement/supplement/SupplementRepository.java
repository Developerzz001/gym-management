package com.gymmanagement.supplement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplementRepository extends JpaRepository<Supplement, Long> {

    List<Supplement> findByClientId(Long clientId);

    List<Supplement> findByDieticianId(Long dieticianId);
}
