package com.gymmanagement.branch;

import com.gymmanagement.branch.dto.*;
import com.gymmanagement.common.dto.ApiResponse;
import com.gymmanagement.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/branches")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ORGANIZATION_ADMIN','BRANCH_MANAGER','RECEPTIONIST')")
@Tag(name = "Branches", description = "Branch administration, configuration and transfers")
public class BranchController {
    private final BranchService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @Operation(summary = "Create a branch")
    public ApiResponse<BranchResponse> create(@Valid @RequestBody BranchRequest request) {
        return ApiResponse.success("Branch created", service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    public ApiResponse<BranchResponse> update(@PathVariable Long id, @Valid @RequestBody BranchRequest request) {
        return ApiResponse.success("Branch updated", service.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    public ApiResponse<BranchResponse> status(@PathVariable Long id, @RequestParam boolean active) {
        return ApiResponse.success(service.setActive(id, active));
    }

    @GetMapping("/{id}")
    public ApiResponse<BranchResponse> get(@PathVariable Long id) { return ApiResponse.success(service.get(id)); }

    @GetMapping
    public ApiResponse<PageResponse<BranchResponse>> list(@RequestParam(required = false) Long organizationId,
            @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.list(organizationId, keyword, page, size));
    }

    @GetMapping("/{id}/settings")
    public ApiResponse<BranchSettingsResponse> settings(@PathVariable Long id) { return ApiResponse.success(service.settings(id)); }

    @PutMapping("/{id}/settings")
    @PreAuthorize("hasAnyRole('ORGANIZATION_ADMIN','BRANCH_MANAGER')")
    public ApiResponse<BranchSettingsResponse> updateSettings(@PathVariable Long id,
            @Valid @RequestBody BranchSettingsRequest request) {
        return ApiResponse.success("Branch settings updated", service.updateSettings(id, request));
    }

    @PostMapping("/members/{clientId}/transfer")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    public ApiResponse<TransferResponse> transferMember(@PathVariable Long clientId,
            @Valid @RequestBody TransferRequest request) {
        return ApiResponse.success("Member transferred", service.transferMember(clientId, request));
    }

    @GetMapping("/members/{clientId}/transfers")
    public ApiResponse<PageResponse<TransferResponse>> memberHistory(@PathVariable Long clientId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.memberHistory(clientId, page, size));
    }

    @PostMapping("/staff/{userId}/transfer")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    public ApiResponse<Void> transferStaff(@PathVariable Long userId, @Valid @RequestBody TransferRequest request) {
        service.transferStaff(userId, request);
        return ApiResponse.message("Staff transferred");
    }
}