package com.gymmanagement.branch;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BranchRepository extends JpaRepository<Branch, Long> {
    @Query(value = """
            WITH code_lock AS (SELECT pg_advisory_xact_lock(68479232))
            SELECT COALESCE(MAX(CAST(SUBSTRING(branch_code FROM 4) AS INTEGER)), 0) + 1
            FROM branches
            CROSS JOIN code_lock
            WHERE organization_id = :organizationId AND branch_code ~ '^BR_[0-9]+$'
            """, nativeQuery = true)
    long findNextGeneratedCodeNumber(@Param("organizationId") Long organizationId);

    Page<Branch> findByOrganizationIdAndBranchNameContainingIgnoreCase(Long organizationId, String keyword, Pageable pageable);
    Page<Branch> findByBranchNameContainingIgnoreCase(String keyword, Pageable pageable);
    List<Branch> findByOrganizationIdAndStatus(Long organizationId, BranchStatus status);
    long countByOrganizationIdAndStatus(Long organizationId, BranchStatus status);
}