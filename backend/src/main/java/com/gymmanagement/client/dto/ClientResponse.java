package com.gymmanagement.client.dto;

import com.gymmanagement.client.Gender;
import com.gymmanagement.client.RegistrationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {

    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private boolean active;
    private boolean membershipActive;
    private boolean membershipAssigned;
    private LocalDate membershipStartDate;
    private LocalDate membershipEndDate;
    private RegistrationType registrationType;

    private Gender gender;
    private LocalDate dateOfBirth;
    private Double heightCm;
    private Double weightKg;
    private String address;
    private String contactNumber;
    private String fitnessGoal;

    private boolean diabetes;
    private boolean hypertension;
    private boolean asthma;
    private String allergies;
    private String injuries;
    private String medicalNotes;

    private Long assignedCoachId;
    private String assignedCoachName;
    private Long assignedDieticianId;
    private String assignedDieticianName;
}
