package com.gymmanagement.branch;

import com.gymmanagement.common.entity.BaseEntity;
import com.gymmanagement.organization.Organization;
import com.gymmanagement.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "branches",
        uniqueConstraints = @UniqueConstraint(name = "uk_branch_organization_code", columnNames = {"organization_id", "branch_code"}),
        indexes = {
                @Index(name = "idx_branch_organization_status", columnList = "organization_id,status"),
                @Index(name = "idx_branch_manager", columnList = "manager_id")
        })
public class Branch extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "branch_code", nullable = false, length = 30)
    private String branchCode;

    @Column(name = "branch_name", nullable = false, length = 150)
    private String branchName;

    @Column(name = "address", nullable = false, length = 500)
    private String address;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "pincode", nullable = false, length = 20)
    private String pincode;

    @Column(name = "contact_number", length = 20)
    private String contactNumber;

    @Column(name = "email", length = 150)
    private String email;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", unique = true)
    private User manager;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BranchStatus status = BranchStatus.ACTIVE;
}