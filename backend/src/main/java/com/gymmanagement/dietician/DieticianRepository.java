package com.gymmanagement.dietician;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DieticianRepository extends JpaRepository<Dietician, Long> {

    Optional<Dietician> findByUserId(Long userId);

    @Query("select d from Dietician d where :keyword is null or " +
            "lower(d.user.firstName) like lower(concat('%', :keyword, '%')) or " +
            "lower(d.user.lastName) like lower(concat('%', :keyword, '%')) or " +
            "lower(d.user.email) like lower(concat('%', :keyword, '%'))")
    Page<Dietician> search(@Param("keyword") String keyword, Pageable pageable);
}
