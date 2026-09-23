package com.gymmanagement.membership;

import com.gymmanagement.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "membership_discounts")
public class MembershipDiscount extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 150)
    private String name;

    @Column(name = "percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage;

    @Builder.Default
    @Column(name = "extra_free_days", nullable = false, columnDefinition = "integer default 0")
    private Integer extraFreeDays = 0;

    @Column(name = "description", length = 500)
    private String description;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;
}