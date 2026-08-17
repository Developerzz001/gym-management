package com.gymmanagement.workout;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {

    List<TrainingSession> findByClientIdOrderBySessionDateTimeDesc(Long clientId);

    List<TrainingSession> findByCoachIdOrderBySessionDateTimeDesc(Long coachId);

    List<TrainingSession> findByClientIdAndStatusOrderBySessionDateTimeAsc(Long clientId, SessionStatus status);
}
