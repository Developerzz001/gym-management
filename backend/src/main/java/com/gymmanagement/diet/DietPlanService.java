package com.gymmanagement.diet;

import com.gymmanagement.diet.dto.DietPlanRequest;
import com.gymmanagement.diet.dto.DietPlanResponse;

import java.util.List;

public interface DietPlanService {

    DietPlanResponse createDietPlan(String dieticianEmail, DietPlanRequest request);

    DietPlanResponse updateDietPlan(String dieticianEmail, Long planId, DietPlanRequest request);

    void deleteDietPlan(String dieticianEmail, Long planId);

    DietPlanResponse getDietPlanById(Long planId);

    List<DietPlanResponse> getPlansByClient(Long clientId);

    List<DietPlanResponse> getPlansByDietician(String dieticianEmail);
}
