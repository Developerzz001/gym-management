package com.gymmanagement.dietician;

import com.gymmanagement.common.dto.ApiResponse;
import com.gymmanagement.common.dto.PageResponse;
import com.gymmanagement.dietician.dto.DieticianRequest;
import com.gymmanagement.dietician.dto.DieticianResponse;
import com.gymmanagement.dietician.dto.DieticianProfileRequest;
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
@RequestMapping("/v1/dieticians")
@RequiredArgsConstructor
@Tag(name = "Dietician Management", description = "Admin APIs to manage dieticians")
public class DieticianController {

    private final DieticianService dieticianService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a new dietician")
    public ApiResponse<DieticianResponse> createDietician(@Valid @RequestBody DieticianRequest request) {
        return ApiResponse.success("Dietician added successfully", dieticianService.createDietician(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing dietician")
    public ApiResponse<DieticianResponse> updateDietician(@PathVariable Long id, @Valid @RequestBody DieticianRequest request) {
        return ApiResponse.success("Dietician updated successfully", dieticianService.updateDietician(id, request));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate a dietician account")
    public ApiResponse<DieticianResponse> activateDietician(@PathVariable Long id) {
        return ApiResponse.success("Dietician activated successfully", dieticianService.activateDietician(id));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a dietician account")
    public ApiResponse<DieticianResponse> deactivateDietician(@PathVariable Long id) {
        return ApiResponse.success("Dietician deactivated successfully", dieticianService.deactivateDietician(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a dietician")
    public ApiResponse<Void> deleteDietician(@PathVariable Long id) {
        dieticianService.deleteDietician(id);
        return ApiResponse.message("Dietician deleted successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DIETICIAN')")
    @Operation(summary = "Get dietician by id")
    public ApiResponse<DieticianResponse> getDietician(@PathVariable Long id) {
        return ApiResponse.success(dieticianService.getDieticianById(id));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('DIETICIAN')")
    public ApiResponse<DieticianResponse> getMyProfile(Authentication authentication) {
        Long userId = userRepository.findByEmailIgnoreCase(authentication.getName()).orElseThrow().getId();
        return ApiResponse.success(dieticianService.getDieticianByUserId(userId));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('DIETICIAN')")
    public ApiResponse<DieticianResponse> updateMyProfile(Authentication authentication,
                                                          @Valid @RequestBody DieticianProfileRequest request) {
        Long userId = userRepository.findByEmailIgnoreCase(authentication.getName()).orElseThrow().getId();
        return ApiResponse.success("Profile updated successfully", dieticianService.updateOwnProfile(userId, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CLIENT')")
    @Operation(summary = "Search / list dieticians with pagination")
    public ApiResponse<PageResponse<DieticianResponse>> getDieticians(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(dieticianService.getDieticians(keyword, page, size));
    }
}
