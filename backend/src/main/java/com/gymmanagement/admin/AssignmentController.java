package com.gymmanagement.admin;

import com.gymmanagement.admin.dto.AssignCoachRequest;
import com.gymmanagement.admin.dto.AssignDieticianRequest;
import com.gymmanagement.client.dto.ClientResponse;
import com.gymmanagement.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/assignments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Assignment Management", description = "Assign/change fitness coach and dietician for a client")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping("/coach")
    @Operation(summary = "Assign or change the fitness coach assigned to a client")
    public ApiResponse<ClientResponse> assignCoach(@Valid @RequestBody AssignCoachRequest request) {
        return ApiResponse.success("Coach assigned successfully", assignmentService.assignCoach(request));
    }

    @PostMapping("/dietician")
    @Operation(summary = "Assign or change the dietician assigned to a client")
    public ApiResponse<ClientResponse> assignDietician(@Valid @RequestBody AssignDieticianRequest request) {
        return ApiResponse.success("Dietician assigned successfully", assignmentService.assignDietician(request));
    }
}
