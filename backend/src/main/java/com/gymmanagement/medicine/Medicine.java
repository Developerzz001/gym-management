package com.gymmanagement.medicine;

import com.gymmanagement.client.Client;
import com.gymmanagement.common.entity.BaseEntity;
import com.gymmanagement.dietician.Dietician;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "medicines")
public class Medicine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dietician_id", nullable = false)
    private Dietician dietician;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "dosage", length = 100)
    private String dosage;

    @Column(name = "timing", length = 100)
    private String timing;

    @Column(name = "instructions", length = 500)
    private String instructions;
}
