package com.gymmanagement.branch;

import com.gymmanagement.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "branch_settings")
public class BranchSettings extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false, unique = true)
    private Branch branch;

    @Column(name = "working_hours", nullable = false, columnDefinition = "TEXT")
    private String workingHours;

    @Builder.Default
    @Column(name = "timezone", nullable = false, length = 60)
    private String timezone = "UTC";

    @Column(name = "membership_rules", columnDefinition = "TEXT")
    private String membershipRules;

    @Column(name = "notification_preferences", columnDefinition = "TEXT")
    private String notificationPreferences;

    @Column(name = "attendance_rules", columnDefinition = "TEXT")
    private String attendanceRules;
}