package com.gymmanagement.membership;

import com.gymmanagement.common.dto.ApiResponse;
import com.gymmanagement.membership.dto.MembershipDiscountRequest;
import com.gymmanagement.membership.dto.MembershipDiscountResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/membership-discounts")
@RequiredArgsConstructor
public class MembershipDiscountController {

    private final MembershipDiscountService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','RECEPTIONIST')")
    public ApiResponse<MembershipDiscountResponse> create(@Valid @RequestBody MembershipDiscountRequest request) {
        return ApiResponse.success("Membership discount created", service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','RECEPTIONIST')")
    public ApiResponse<MembershipDiscountResponse> update(@PathVariable Long id,
            @Valid @RequestBody MembershipDiscountRequest request) {
        return ApiResponse.success("Membership discount updated", service.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','RECEPTIONIST')")
    public ApiResponse<MembershipDiscountResponse> status(@PathVariable Long id, @RequestParam boolean active) {
        return ApiResponse.success(service.setActive(id, active));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','RECEPTIONIST')")
    public ApiResponse<List<MembershipDiscountResponse>> list(
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        return ApiResponse.success(service.list(activeOnly));
    }
}