package com.gymmanagement.enrollment;

import com.gymmanagement.common.dto.ApiResponse;
import com.gymmanagement.enrollment.dto.ClientEnrollmentRequest;
import com.gymmanagement.enrollment.dto.EnrollmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/enrollments")
@RequiredArgsConstructor
@Tag(name = "Client Enrollment", description = "Two-step client registration with membership enrollment and payment")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/clients")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','ORGANIZATION_ADMIN','BRANCH_MANAGER','RECEPTIONIST')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new client and enroll them into a membership plan with payment")
    public ApiResponse<EnrollmentResponse> registerClientWithEnrollment(@Valid @RequestBody ClientEnrollmentRequest request) {
        return ApiResponse.success("Client registered and enrolled successfully",
                enrollmentService.registerClientWithEnrollment(request));
    }

    @PostMapping("/clients/{clientId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','ORGANIZATION_ADMIN','BRANCH_MANAGER','RECEPTIONIST')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Enroll an existing client into a membership plan with payment (renewal/upgrade/transfer)")
    public ApiResponse<EnrollmentResponse> enrollExistingClient(@PathVariable Long clientId,
                                                                 @Valid @RequestBody ClientEnrollmentRequest request) {
        return ApiResponse.success("Client enrolled successfully",
                enrollmentService.enrollExistingClient(clientId, request));
    }
}
