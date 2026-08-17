package com.gymmanagement.progress.dto;

import jakarta.validation.constraints.NotNull;
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
public class ProgressRecordRequest {

    @NotNull(message = "Client id is required")
    private Long clientId;

    @NotNull(message = "Record date is required")
    private LocalDate recordDate;

    private Double weightKg;

    private Double chestCm;

    private Double waistCm;

    private Double armsCm;

    private Double shoulderCm;

    private Double thighCm;
}
