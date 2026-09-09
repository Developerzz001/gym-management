package com.gymmanagement.coach;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FitnessCoachRepository extends JpaRepository<FitnessCoach, Long> {

    Optional<FitnessCoach> findByUserId(Long userId);

    @Query("select c from FitnessCoach c where " +
            "lower(c.user.firstName) like lower(concat('%', :keyword, '%')) or " +
            "lower(c.user.lastName) like lower(concat('%', :keyword, '%')) or " +
            "lower(c.user.email) like lower(concat('%', :keyword, '%'))")
    Page<FitnessCoach> search(@Param("keyword") String keyword, Pageable pageable);
}
