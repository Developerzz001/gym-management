package com.gymmanagement.client;

import com.gymmanagement.client.dto.ClientRequest;
import com.gymmanagement.client.dto.ClientResponse;
import com.gymmanagement.common.dto.ApiResponse;
import com.gymmanagement.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/clients")
@RequiredArgsConstructor
@Tag(name = "Client Management", description = "Manage client registration and profiles")
public class ClientController {

    private final ClientService clientService;
    private final com.gymmanagement.user.UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new client")
    public ApiResponse<ClientResponse> registerClient(@Valid @RequestBody ClientRequest request) {
        return ApiResponse.success("Client registered successfully", clientService.registerClient(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update client profile")
    public ApiResponse<ClientResponse> updateClient(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        return ApiResponse.success("Client updated successfully", clientService.updateClient(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a client account")
    public ApiResponse<ClientResponse> deactivateClient(@PathVariable Long id) {
        return ApiResponse.success("Client deactivated successfully", clientService.deactivateClient(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FITNESS_COACH','DIETICIAN')")
    @Operation(summary = "Get client by id")
    public ApiResponse<ClientResponse> getClient(@PathVariable Long id) {
        return ApiResponse.success(clientService.getClientById(id));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Get the currently logged-in client's own profile")
    public ApiResponse<ClientResponse> getMyProfile(Authentication authentication) {
        Long userId = userRepository.findByEmailIgnoreCase(authentication.getName()).orElseThrow().getId();
        return ApiResponse.success(clientService.getClientByUserId(userId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search / list clients with pagination")
    public ApiResponse<PageResponse<ClientResponse>> getClients(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(clientService.getClients(keyword, page, size));
    }

    @GetMapping("/assigned-coach")
    @PreAuthorize("hasRole('FITNESS_COACH')")
    @Operation(summary = "Get clients assigned to the currently logged-in coach")
    public ApiResponse<java.util.List<ClientResponse>> getMyAssignedClientsAsCoach(Authentication authentication) {
        return ApiResponse.success(clientService.getClientsByCoach(authentication.getName()));
    }

    @GetMapping("/assigned-dietician")
    @PreAuthorize("hasRole('DIETICIAN')")
    @Operation(summary = "Get clients assigned to the currently logged-in dietician")
    public ApiResponse<java.util.List<ClientResponse>> getMyAssignedClientsAsDietician(Authentication authentication) {
        return ApiResponse.success(clientService.getClientsByDietician(authentication.getName()));
    }
}
