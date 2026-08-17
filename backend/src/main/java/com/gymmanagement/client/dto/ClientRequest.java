package com.gymmanagement.client.dto;

import com.gymmanagement.client.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class ClientRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    private String password;

    private Boolean active;

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
