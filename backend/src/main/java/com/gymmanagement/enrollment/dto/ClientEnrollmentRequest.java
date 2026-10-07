package com.gymmanagement.enrollment.dto;

import com.gymmanagement.billing.PaymentMethod;
import com.gymmanagement.client.Gender;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Combines Step 1 (client profile) and Step 2 (membership + payment) fields for a single enrollment transaction. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientEnrollmentRequest {

    private Long branchId;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String email;

    @NotBlank(message = "Mobile number is required")
    private String contactNumber;

    private String alternateContactNumber;

    @NotNull(message = "Gender is required")
    private Gender gender;

    private LocalDate dateOfBirth;
    private String address;
    private Long executiveId;
    private String fitnessGoal;
    private Double heightCm;
    private Double weightKg;
    private String allergies;
    private String injuries;
    private String medicalNotes;

    @NotNull(message = "Sport / Activity is required")
    private Long activityId;

    @NotNull(message = "Membership plan is required")
    private Long membershipPlanId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private Long membershipDiscountId;

    @NotNull(message = "Tax is required")
    @DecimalMin(value = "0.00", message = "Tax cannot be negative")
    private BigDecimal tax;

    private String description;

    @NotNull(message = "Amount paid is required")
    @DecimalMin(value = "0.01", message = "Amount paid must be greater than zero")
    private BigDecimal amountPaid;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String transactionReference;
    private String remarks;
}
