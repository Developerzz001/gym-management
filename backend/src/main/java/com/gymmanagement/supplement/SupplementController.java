package com.gymmanagement.supplement;

import com.gymmanagement.common.dto.ApiResponse;
import com.gymmanagement.supplement.dto.SupplementRequest;
import com.gymmanagement.supplement.dto.SupplementResponse;
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
@RequestMapping("/v1/supplements")
@RequiredArgsConstructor
@Tag(name = "Supplements", description = "Dietician prescribes supplements for assigned clients")
public class SupplementController {

    private final SupplementService supplementService;

    @PostMapping
    @PreAuthorize("hasRole('DIETICIAN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a supplement recommendation for a client")
    public ApiResponse<SupplementResponse> create(Authentication authentication, @Valid @RequestBody SupplementRequest request) {
        return ApiResponse.success("Supplement added successfully", supplementService.createSupplement(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DIETICIAN')")
    @Operation(summary = "Update a supplement recommendation")
    public ApiResponse<SupplementResponse> update(Authentication authentication, @PathVariable Long id,
                                                   @Valid @RequestBody SupplementRequest request) {
        return ApiResponse.success("Supplement updated successfully", supplementService.updateSupplement(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DIETICIAN')")
    @Operation(summary = "Delete a supplement recommendation")
    public ApiResponse<Void> delete(Authentication authentication, @PathVariable Long id) {
        supplementService.deleteSupplement(authentication.getName(), id);
        return ApiResponse.message("Supplement deleted successfully");
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get all supplements assigned to a client")
    public ApiResponse<List<SupplementResponse>> getByClient(@PathVariable Long clientId) {
        return ApiResponse.success(supplementService.getByClient(clientId));
    }
}
