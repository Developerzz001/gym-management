package com.gymmanagement.progress;

import com.gymmanagement.common.dto.ApiResponse;
import com.gymmanagement.progress.dto.ProgressRecordRequest;
import com.gymmanagement.progress.dto.ProgressRecordResponse;
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
@RequestMapping("/v1/progress")
@RequiredArgsConstructor
@Tag(name = "Progress Tracking", description = "Date-wise tracking of client body metrics, updated by the coach")
public class ProgressRecordController {

    private final ProgressRecordService progressRecordService;

    @PostMapping
    @PreAuthorize("hasRole('FITNESS_COACH')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a new progress record for a client")
    public ApiResponse<ProgressRecordResponse> add(Authentication authentication, @Valid @RequestBody ProgressRecordRequest request) {
        return ApiResponse.success("Progress record added successfully", progressRecordService.addProgress(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FITNESS_COACH')")
    @Operation(summary = "Update an existing progress record")
    public ApiResponse<ProgressRecordResponse> update(Authentication authentication, @PathVariable Long id,
                                                       @Valid @RequestBody ProgressRecordRequest request) {
        return ApiResponse.success("Progress record updated successfully", progressRecordService.updateProgress(authentication.getName(), id, request));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get date-wise progress history for a client")
    public ApiResponse<List<ProgressRecordResponse>> getByClient(@PathVariable Long clientId) {
        return ApiResponse.success(progressRecordService.getByClient(clientId));
    }
}
