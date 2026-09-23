package com.gymmanagement.workout;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {

    List<TrainingSession> findByClientIdOrderBySessionDateTimeDesc(Long clientId);

    List<TrainingSession> findByCoachIdOrderBySessionDateTimeDesc(Long coachId);

    List<TrainingSession> findByClientIdAndStatusOrderBySessionDateTimeAsc(Long clientId, SessionStatus status);

    long countByStatusAndSessionDateTimeBetween(SessionStatus status, LocalDateTime start, LocalDateTime end);

    @Query("select s.coach.id, s.coach.user.firstName, s.coach.user.lastName, count(s) from TrainingSession s " +
            "where s.status = com.gymmanagement.workout.SessionStatus.COMPLETED and s.sessionDateTime between :start and :end " +
            "group by s.coach.id, s.coach.user.firstName, s.coach.user.lastName order by count(s) desc")
    List<Object[]> topCoaches(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                              org.springframework.data.domain.Pageable pageable);
}
