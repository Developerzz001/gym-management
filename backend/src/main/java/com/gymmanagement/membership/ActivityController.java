package com.gymmanagement.membership;

import com.gymmanagement.common.dto.ApiResponse;
import com.gymmanagement.membership.dto.ActivityRequest;
import com.gymmanagement.membership.dto.ActivityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/activities")
@RequiredArgsConstructor
@Tag(name = "Sport / Activity", description = "Admin manages sport/activity master data")
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','RECEPTIONIST')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new sport/activity")
    public ApiResponse<ActivityResponse> create(@Valid @RequestBody ActivityRequest request) {
        return ApiResponse.success("Sport/Activity created successfully", activityService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','RECEPTIONIST')")
    @Operation(summary = "Update a sport/activity")
    public ApiResponse<ActivityResponse> update(@PathVariable Long id, @Valid @RequestBody ActivityRequest request) {
        return ApiResponse.success("Sport/Activity updated successfully", activityService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','RECEPTIONIST')")
    @Operation(summary = "Delete a sport/activity")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        activityService.delete(id);
        return ApiResponse.message("Sport/Activity deleted successfully");
    }

    @GetMapping
    @Operation(summary = "List all sports/activities")
    public ApiResponse<List<ActivityResponse>> getAll() {
        return ApiResponse.success(activityService.getAll());
    }
}
