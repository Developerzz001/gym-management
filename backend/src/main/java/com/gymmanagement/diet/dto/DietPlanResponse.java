package com.gymmanagement.diet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DietPlanResponse {

    private Long id;
    private Long clientId;
    private String clientName;
    private Long dieticianId;
    private String dieticianName;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private List<DietPlanDetailResponse> details;
}
