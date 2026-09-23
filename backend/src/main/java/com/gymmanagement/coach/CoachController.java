package com.gymmanagement.coach;

import com.gymmanagement.common.dto.ApiResponse;
import com.gymmanagement.common.dto.PageResponse;
import com.gymmanagement.coach.dto.CoachRequest;
import com.gymmanagement.coach.dto.CoachResponse;
import com.gymmanagement.coach.dto.CoachProfileRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.gymmanagement.user.UserRepository;

@RestController
@RequestMapping("/v1/coaches")
@RequiredArgsConstructor
@Tag(name = "Coach Management", description = "Admin APIs to manage fitness coaches")
public class CoachController {

    private final CoachService coachService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a new fitness coach")
    public ApiResponse<CoachResponse> createCoach(@Valid @RequestBody CoachRequest request) {
        return ApiResponse.success("Coach added successfully", coachService.createCoach(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing fitness coach")
    public ApiResponse<CoachResponse> updateCoach(@PathVariable Long id, @Valid @RequestBody CoachRequest request) {
        return ApiResponse.success("Coach updated successfully", coachService.updateCoach(id, request));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate a fitness coach account")
    public ApiResponse<CoachResponse> activateCoach(@PathVariable Long id) {
        return ApiResponse.success("Coach activated successfully", coachService.activateCoach(id));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a fitness coach account")
    public ApiResponse<CoachResponse> deactivateCoach(@PathVariable Long id) {
        return ApiResponse.success("Coach deactivated successfully", coachService.deactivateCoach(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a fitness coach")
    public ApiResponse<Void> deleteCoach(@PathVariable Long id) {
        coachService.deleteCoach(id);
        return ApiResponse.message("Coach deleted successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FITNESS_COACH','COACH')")
    @Operation(summary = "Get fitness coach by id")
    public ApiResponse<CoachResponse> getCoach(@PathVariable Long id) {
        return ApiResponse.success(coachService.getCoachById(id));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('FITNESS_COACH','COACH')")
    public ApiResponse<CoachResponse> getMyProfile(Authentication authentication) {
        Long userId = userRepository.findByEmailIgnoreCase(authentication.getName()).orElseThrow().getId();
        return ApiResponse.success(coachService.getCoachByUserId(userId));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('FITNESS_COACH','COACH')")
    public ApiResponse<CoachResponse> updateMyProfile(Authentication authentication,
                                                       @Valid @RequestBody CoachProfileRequest request) {
        Long userId = userRepository.findByEmailIgnoreCase(authentication.getName()).orElseThrow().getId();
        return ApiResponse.success("Profile updated successfully", coachService.updateOwnProfile(userId, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CLIENT')")
    @Operation(summary = "Search / list fitness coaches with pagination")
    public ApiResponse<PageResponse<CoachResponse>> getCoaches(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(coachService.getCoaches(keyword, page, size));
    }
}
