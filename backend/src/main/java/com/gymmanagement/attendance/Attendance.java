package com.gymmanagement.attendance;

import com.gymmanagement.branch.Branch;
import com.gymmanagement.client.Client;
import com.gymmanagement.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "attendance", indexes = {
        @Index(name = "idx_attendance_client_checkin", columnList = "client_id,check_in_at"),
    @Index(name = "idx_attendance_checkin", columnList = "check_in_at"),
    @Index(name = "idx_attendance_branch_checkin", columnList = "branch_id,check_in_at")
}, uniqueConstraints = @UniqueConstraint(name = "uk_attendance_active_client", columnNames = "active_key"))
public class Attendance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(name = "check_in_at", nullable = false)
    private LocalDateTime checkInAt;

    @Column(name = "check_out_at")
    private LocalDateTime checkOutAt;

    @Column(name = "duration_minutes")
    private Long durationMinutes;

    @Column(name = "active_key", unique = true, length = 50)
    private String activeKey;
}