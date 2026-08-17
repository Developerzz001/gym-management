package com.gymmanagement.workout;

import com.gymmanagement.common.dto.ApiResponse;
import com.gymmanagement.workout.dto.SessionRequest;
import com.gymmanagement.workout.dto.SessionResponse;
import com.gymmanagement.workout.dto.SessionStatusUpdateRequest;
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
@RequestMapping("/v1/sessions")
@RequiredArgsConstructor
@Tag(name = "Training Sessions", description = "Schedule and manage personal training sessions")
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    @PreAuthorize("hasRole('FITNESS_COACH')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Schedule a new personal training session")
    public ApiResponse<SessionResponse> scheduleSession(Authentication authentication,
                                                         @Valid @RequestBody SessionRequest request) {
        return ApiResponse.success("Session scheduled successfully",
                sessionService.scheduleSession(authentication.getName(), request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('FITNESS_COACH')")
    @Operation(summary = "Update the status of a training session")
    public ApiResponse<SessionResponse> updateStatus(Authentication authentication, @PathVariable Long id,
                                                      @Valid @RequestBody SessionStatusUpdateRequest request) {
        return ApiResponse.success("Session status updated successfully",
                sessionService.updateSessionStatus(authentication.getName(), id, request));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get all sessions for a specific client")
    public ApiResponse<List<SessionResponse>> getSessionsByClient(@PathVariable Long clientId) {
        return ApiResponse.success(sessionService.getSessionsByClient(clientId));
    }

    @GetMapping("/client/{clientId}/upcoming")
    @Operation(summary = "Get upcoming (scheduled) sessions for a client")
    public ApiResponse<List<SessionResponse>> getUpcomingSessions(@PathVariable Long clientId) {
        return ApiResponse.success(sessionService.getUpcomingSessionsForClient(clientId));
    }

    @GetMapping("/my-sessions")
    @PreAuthorize("hasRole('FITNESS_COACH')")
    @Operation(summary = "Get all sessions scheduled by the currently logged-in coach")
    public ApiResponse<List<SessionResponse>> getMySessions(Authentication authentication) {
        return ApiResponse.success(sessionService.getSessionsByCoach(authentication.getName()));
    }
}
