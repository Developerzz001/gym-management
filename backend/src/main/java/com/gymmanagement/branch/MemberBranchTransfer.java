package com.gymmanagement.branch;

import com.gymmanagement.client.Client;
import com.gymmanagement.common.entity.BaseEntity;
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
@Table(name = "member_branch_transfers", indexes = {
        @Index(name = "idx_member_transfer_client_created", columnList = "client_id,created_at"),
        @Index(name = "idx_member_transfer_branches", columnList = "from_branch_id,to_branch_id")
})
public class MemberBranchTransfer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_branch_id", nullable = false)
    private Branch fromBranch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_branch_id", nullable = false)
    private Branch toBranch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transferred_by", nullable = false)
    private User transferredBy;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;
}