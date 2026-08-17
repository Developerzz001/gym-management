package com.gymmanagement.medicine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineRequest {

    @NotNull(message = "Client id is required")
    private Long clientId;

    @NotBlank(message = "Name is required")
    private String name;

    private String dosage;

    private String timing;

    private String instructions;
}
