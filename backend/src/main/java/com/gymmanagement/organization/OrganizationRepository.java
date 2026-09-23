package com.gymmanagement.organization;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    @Query(value = """
            WITH code_lock AS (SELECT pg_advisory_xact_lock(68479231))
            SELECT COALESCE(MAX(CAST(SUBSTRING(code FROM 5) AS INTEGER)), 0) + 1
            FROM organizations
            CROSS JOIN code_lock
            WHERE code ~ '^ORG_[0-9]+$'
            """, nativeQuery = true)
    long findNextGeneratedCodeNumber();

    Page<Organization> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(String name, String code, Pageable pageable);
}