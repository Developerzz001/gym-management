package com.gymmanagement.client;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowUpRepository extends JpaRepository<FollowUp, Long> {

    List<FollowUp> findByClientIdOrderByFollowUpDateDescCreatedAtDesc(Long clientId);
}
