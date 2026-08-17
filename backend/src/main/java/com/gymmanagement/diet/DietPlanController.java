package com.gymmanagement.diet;

import com.gymmanagement.common.dto.ApiResponse;
import com.gymmanagement.diet.dto.DietPlanRequest;
import com.gymmanagement.diet.dto.DietPlanResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/diet-plans")
@RequiredArgsConstructor
@Tag(name = "Diet Plans", description = "Dietician creates/updates diet plans for assigned clients")
public class DietPlanController {

    private final DietPlanService dietPlanService;

    @PostMapping
    @PreAuthorize("hasRole('DIETICIAN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new diet plan for an assigned client")
    public ApiResponse<DietPlanResponse> createPlan(Authentication authentication,
                                                     @Valid @RequestBody DietPlanRequest request) {
        return ApiResponse.success("Diet plan created successfully",
                dietPlanService.createDietPlan(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DIETICIAN')")
    @Operation(summary = "Update an existing diet plan")
    public ApiResponse<DietPlanResponse> updatePlan(Authentication authentication, @PathVariable Long id,
                                                     @Valid @RequestBody DietPlanRequest request) {
        return ApiResponse.success("Diet plan updated successfully",
                dietPlanService.updateDietPlan(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DIETICIAN')")
    @Operation(summary = "Delete a diet plan")
    public ApiResponse<Void> deletePlan(Authentication authentication, @PathVariable Long id) {
        dietPlanService.deleteDietPlan(authentication.getName(), id);
        return ApiResponse.message("Diet plan deleted successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get diet plan by id")
    public ApiResponse<DietPlanResponse> getPlan(@PathVariable Long id) {
        return ApiResponse.success(dietPlanService.getDietPlanById(id));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get all diet plans for a specific client")
    public ApiResponse<List<DietPlanResponse>> getPlansByClient(@PathVariable Long clientId) {
        return ApiResponse.success(dietPlanService.getPlansByClient(clientId));
    }

    @GetMapping("/my-plans")
    @PreAuthorize("hasRole('DIETICIAN')")
    @Operation(summary = "Get all diet plans created by the currently logged-in dietician")
    public ApiResponse<List<DietPlanResponse>> getMyPlans(Authentication authentication) {
        return ApiResponse.success(dietPlanService.getPlansByDietician(authentication.getName()));
    }
}
