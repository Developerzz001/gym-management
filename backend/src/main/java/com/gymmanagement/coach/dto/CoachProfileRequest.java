package com.gymmanagement.coach.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoachProfileRequest {
    private String firstName;
    private String lastName;

    @Email(message = "Email must be valid")
    private String email;

    @Pattern(regexp = "^[0-9+\\-\\s]{7,20}$", message = "Mobile number is invalid")
    private String mobileNumber;

    private String password;
    private String specialization;
    private Integer experienceYears;
    private String bio;
}