package com.gymmanagement.progress.dto;

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
public class ProgressRecordResponse {

    private Long id;
    private Long clientId;
    private LocalDate recordDate;
    private Double weightKg;
    private Double bmi;
    private Double chestCm;
    private Double waistCm;
    private Double armsCm;
    private Double shoulderCm;
    private Double thighCm;
}
