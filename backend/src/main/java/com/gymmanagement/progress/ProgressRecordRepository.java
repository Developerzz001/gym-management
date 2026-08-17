package com.gymmanagement.progress;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgressRecordRepository extends JpaRepository<ProgressRecord, Long> {

    List<ProgressRecord> findByClientIdOrderByRecordDateDesc(Long clientId);
}
