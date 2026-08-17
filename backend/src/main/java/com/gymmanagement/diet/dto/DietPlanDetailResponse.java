package com.gymmanagement.diet.dto;

import com.gymmanagement.diet.MealType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DietPlanDetailResponse {

    private Long id;
    private MealType mealType;
    private String foodItem;
    private String quantity;
    private Integer calories;
    private LocalTime mealTime;
}
