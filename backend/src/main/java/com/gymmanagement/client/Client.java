package com.gymmanagement.client;

import com.gymmanagement.coach.FitnessCoach;
import com.gymmanagement.common.entity.BaseEntity;
import com.gymmanagement.dietician.Dietician;
import com.gymmanagement.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "clients")
public class Client extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "height_cm")
    private Double heightCm;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "contact_number", length = 20)
    private String contactNumber;

    @Column(name = "fitness_goal", length = 255)
    private String fitnessGoal;

    // Medical details
    @Builder.Default
    @Column(name = "diabetes", nullable = false)
    private boolean diabetes = false;

    @Builder.Default
    @Column(name = "hypertension", nullable = false)
    private boolean hypertension = false;

    @Builder.Default
    @Column(name = "asthma", nullable = false)
    private boolean asthma = false;

    @Column(name = "allergies", length = 500)
    private String allergies;

    @Column(name = "injuries", length = 500)
    private String injuries;

    @Column(name = "medical_notes", length = 1000)
    private String medicalNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_coach_id")
    private FitnessCoach assignedCoach;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_dietician_id")
    private Dietician assignedDietician;
}
