package com.gymmanagement.diet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DietPlanRepository extends JpaRepository<DietPlan, Long> {

    List<DietPlan> findByClientId(Long clientId);

    List<DietPlan> findByDieticianId(Long dieticianId);

    boolean existsByClientIdAndDieticianId(Long clientId, Long dieticianId);

    java.util.Optional<DietPlan> findTopByClientIdOrderByCreatedAtDesc(Long clientId);
}
