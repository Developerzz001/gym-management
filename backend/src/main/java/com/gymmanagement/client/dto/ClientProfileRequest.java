package com.gymmanagement.client.dto;

import com.gymmanagement.client.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ClientProfileRequest {
    private String firstName;
    private String lastName;

    @Email(message = "Email must be valid")
    private String email;

    private String password;
    private Gender gender;
    private LocalDate dateOfBirth;
    private Double heightCm;
    private Double weightKg;
    private String address;

    @Pattern(regexp = "^[0-9+\\-\\s]{7,20}$", message = "Contact number is invalid")
    private String contactNumber;

    private String fitnessGoal;
    private boolean diabetes;
    private boolean hypertension;
    private boolean asthma;
    private String allergies;
    private String injuries;
    private String medicalNotes;
}