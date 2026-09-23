package com.gymmanagement.branch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BranchSettingsRepository extends JpaRepository<BranchSettings, Long> {
    Optional<BranchSettings> findByBranchId(Long branchId);
}