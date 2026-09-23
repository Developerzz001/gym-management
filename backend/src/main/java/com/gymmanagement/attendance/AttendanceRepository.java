package com.gymmanagement.attendance;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.*;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByClientIdAndCheckOutAtIsNull(Long clientId);

    Page<Attendance> findByClientId(Long clientId, Pageable pageable);

    List<Attendance> findByCheckInAtBetweenOrderByCheckInAtDesc(LocalDateTime start, LocalDateTime end);

    long countByCheckInAtBetween(LocalDateTime start, LocalDateTime end);

        long countByBranchIdAndCheckInAtBetween(Long branchId, LocalDateTime start, LocalDateTime end);

        long countByBranchOrganizationIdAndCheckInAtBetween(Long organizationId, LocalDateTime start, LocalDateTime end);

    @Query("select function('hour', a.checkInAt), count(a) from Attendance a " +
            "where a.checkInAt between :start and :end group by function('hour', a.checkInAt) order by count(a) desc")
    List<Object[]> peakHours(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    @Query("select count(a), coalesce(sum(a.durationMinutes), 0) from Attendance a " +
            "where a.client.id = :clientId and a.checkInAt between :start and :end")
    Object[] usage(@Param("clientId") Long clientId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}