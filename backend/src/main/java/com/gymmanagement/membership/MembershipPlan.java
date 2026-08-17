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
@Table(name = "membership_plans")
public class MembershipPlan extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "fees", nullable = false, precision = 10, scale = 2)
    private BigDecimal fees;

    @Column(name = "description", length = 500)
    private String description;
}
