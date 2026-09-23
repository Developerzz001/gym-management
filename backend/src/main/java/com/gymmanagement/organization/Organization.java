package com.gymmanagement.organization;

import com.gymmanagement.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
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
@Table(name = "organizations", indexes = {
        @Index(name = "idx_organization_status", columnList = "status")
})
public class Organization extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "owner_name", nullable = false, length = 150)
    private String ownerName;

    @Column(name = "contact_number", length = 20)
    private String contactNumber;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "address", length = 500)
    private String address;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrganizationStatus status = OrganizationStatus.ACTIVE;
}