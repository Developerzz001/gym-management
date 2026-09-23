package com.gymmanagement.organization;

import com.gymmanagement.common.dto.ApiResponse;
import com.gymmanagement.common.dto.PageResponse;
import com.gymmanagement.organization.dto.OrganizationRequest;
import com.gymmanagement.organization.dto.OrganizationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/organizations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "Organizations", description = "Organization lifecycle management")
public class OrganizationController {
    private final OrganizationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an organization")
    public ApiResponse<OrganizationResponse> create(@Valid @RequestBody OrganizationRequest request) {
        return ApiResponse.success("Organization created", service.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<OrganizationResponse> update(@PathVariable Long id, @Valid @RequestBody OrganizationRequest request) {
        return ApiResponse.success("Organization updated", service.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<OrganizationResponse> status(@PathVariable Long id, @RequestParam boolean active) {
        return ApiResponse.success(service.setActive(id, active));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrganizationResponse> get(@PathVariable Long id) { return ApiResponse.success(service.get(id)); }

    @GetMapping
    public ApiResponse<PageResponse<OrganizationResponse>> list(@RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.list(keyword, page, size));
    }
}