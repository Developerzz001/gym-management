package com.gymmanagement.diet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DietPlanRepository extends JpaRepository<DietPlan, Long> {

    List<DietPlan> findByClientId(Long clientId);

    List<DietPlan> findByDieticianId(Long dieticianId);

    boolean existsByClientIdAndDieticianId(Long clientId, Long dieticianId);

    java.util.Optional<DietPlan> findTopByClientIdOrderByCreatedAtDesc(Long clientId);

    @Query("select d.dietician.id, d.dietician.user.firstName, d.dietician.user.lastName, count(d) from DietPlan d " +
            "where d.createdAt between :start and :end group by d.dietician.id, d.dietician.user.firstName, " +
            "d.dietician.user.lastName order by count(d) desc")
    List<Object[]> topDieticians(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                 org.springframework.data.domain.Pageable pageable);
}
