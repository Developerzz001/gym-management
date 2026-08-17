package com.gymmanagement.medicine;

import com.gymmanagement.common.dto.ApiResponse;
import com.gymmanagement.medicine.dto.MedicineRequest;
import com.gymmanagement.medicine.dto.MedicineResponse;
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
@RequestMapping("/v1/medicines")
@RequiredArgsConstructor
@Tag(name = "Medicines", description = "Dietician prescribes medicines for assigned clients")
public class MedicineController {

    private final MedicineService medicineService;

    @PostMapping
    @PreAuthorize("hasRole('DIETICIAN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a medicine prescription for a client")
    public ApiResponse<MedicineResponse> create(Authentication authentication, @Valid @RequestBody MedicineRequest request) {
        return ApiResponse.success("Medicine added successfully", medicineService.createMedicine(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DIETICIAN')")
    @Operation(summary = "Update a medicine prescription")
    public ApiResponse<MedicineResponse> update(Authentication authentication, @PathVariable Long id,
                                                 @Valid @RequestBody MedicineRequest request) {
        return ApiResponse.success("Medicine updated successfully", medicineService.updateMedicine(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DIETICIAN')")
    @Operation(summary = "Delete a medicine prescription")
    public ApiResponse<Void> delete(Authentication authentication, @PathVariable Long id) {
        medicineService.deleteMedicine(authentication.getName(), id);
        return ApiResponse.message("Medicine deleted successfully");
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get all medicines assigned to a client")
    public ApiResponse<List<MedicineResponse>> getByClient(@PathVariable Long clientId) {
        return ApiResponse.success(medicineService.getByClient(clientId));
    }
}
