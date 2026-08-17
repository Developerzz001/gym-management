package com.gymmanagement.supplement.dto;

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
public class SupplementResponse {

    private Long id;
    private Long clientId;
    private String clientName;
    private Long dieticianId;
    private String name;
    private String dosage;
    private String timing;
    private String instructions;
}
