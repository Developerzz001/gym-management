package com.gymmanagement.audit;

import com.gymmanagement.branch.Branch;
import com.gymmanagement.common.entity.BaseEntity;
import com.gymmanagement.organization.Organization;
import com.gymmanagement.user.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "branch_audit_logs", indexes = {
        @Index(name = "idx_branch_audit_branch_created", columnList = "branch_id,created_at"),
        @Index(name = "idx_branch_audit_org_created", columnList = "organization_id,created_at")
})
public class BranchAuditLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "action", nullable = false, length = 60)
    private String action;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;
}